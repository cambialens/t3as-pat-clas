/*
    Copyright 2013, 2014 NICTA
    
    This file is part of t3as (Text Analysis As A Service).

    t3as is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    t3as is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with t3as.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.t3as.patClas.service

import java.io.File
import javax.ws.rs.core.MediaType
import javax.ws.rs.{Path, _}

import io.swagger.annotations.{Api, SwaggerDefinition, Info}
import org.apache.commons.dbcp2.BasicDataSource
import org.apache.lucene.store.{Directory, FSDirectory}
import org.slf4j.LoggerFactory
import org.t3as.patClas.api.API.{Factory, LookupService, SearchService}
import org.t3as.patClas.api._
import org.t3as.patClas.api.javaApi.{Factory => JF}
import org.t3as.patClas.common.search._

import scala.language.implicitConversions
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend.Database


object PatClasService {
  var s: Option[PatClasService] = None

  /** methods invoked by ServletListener configured in web.xml */
  def init = s = Some(new PatClasService)

  def close = s.map(_.close)

  def testInit(p: PatClasService) = s = Some(p)

  def service = s.getOrElse(throw new Exception("not initialised"))
}

class PatClasService {

  import org.t3as.patClas.common.Util
  import Util.get

  val log = LoggerFactory.getLogger(getClass)

  log.debug("PatClasService:ctor")

  implicit val props = Util.properties("/patClasService.properties")

  val datasource = {
    val ds = new BasicDataSource
    ds.setDriverClassName(get("jdbc.driver"))
    ds.setUsername(get("jdbc.username"))
    ds.setPassword(get("jdbc.password"))
    ds.setMaxTotal(get("jdbc.maxActive").toInt);
    ds.setMaxIdle(get("jdbc.maxIdle").toInt);
    ds.setInitialSize(get("jdbc.initialSize").toInt);
    ds.setValidationQuery(get("jdbc.validationQuery"))
    ds.setPoolPreparedStatements(true) // slick relies on this to reuse prepared statements
    ds.setUrl(get("jdbc.url"))

    // test the data source validity
    ds.getConnection().close()
    ds
  }

  val database = Database.forDataSource(datasource)

  val slickDriver: JdbcProfile = Util.getObject(get("slick.driver"))

  import org.t3as.patClas.common.db.{CPCdb, IPCdb, USPCdb}

  val cpcDb = new CPCdb(slickDriver)
  val ipcDb = new IPCdb(slickDriver)
  val uspcDb = new USPCdb(slickDriver)

  /** Get a function to transform xml text.
    * If f == Format.XML return the null transform (which preserves the markup), else return toText (which strips out the tags leaving just the text).
    */
  def getToText(f: String) = if ("xml".equalsIgnoreCase(f)) (xml: String) => xml else Util.toText _

  // tests can override with a RAMDirectory
  def indexDir(prop: String): Directory = FSDirectory.open(new File(get(prop)))

  def mkCombinedSuggest(indexDir: File) = {
    import Suggest._

    val exact = new ExactSuggest
    exact.load(exactSugFile(indexDir))

    val fuzzy = new FuzzySuggest
    fuzzy.load(fuzzySugFile(indexDir))

    (key: String, num: Int) => {
      val x = exact.lookup(key, num)
      val n = num - x.size
      val f = if (n > 0) {
        val xs = x.toSet
        // fuzzy also gets exact matches, so filter them out
        fuzzy.lookup(key, n + xs.size).filter(!xs.contains(_)).take(n)
      } else List.empty
      Suggestions(x, f)
    }
  }

  val cpcSearcher = {
    import org.t3as.patClas.common.CPCUtil._
    new Searcher[CPCHit](textFields, unstemmedTextFields, Constants.cpcAnalyzer, hitFields, indexDir("cpc.index.path"), mkHit)
  }

  val cpcSuggest = mkCombinedSuggest(new File(get("cpc.index.path")))

  val ipcSearcher = {
    import org.t3as.patClas.common.IPCUtil._
    new Searcher[IPCHit](textFields, unstemmedTextFields, Constants.ipcAnalyzer, hitFields, indexDir("ipc.index.path"), mkHit)
  }

  val ipcSuggest = mkCombinedSuggest(new File(get("ipc.index.path")))

  val uspcSearcher = {
    import org.t3as.patClas.common.USPCUtil._
    new Searcher[USPCHit](textFields, unstemmedTextFields, Constants.uspcAnalyzer, hitFields, indexDir("uspc.index.path"), mkHit)
  }

  val uspcSuggest = mkCombinedSuggest(new File(get("uspc.index.path")))

  def close = {
    log.info("Closing datasource and search indices")
    datasource.close
    cpcSearcher.close
    ipcSearcher.close
    uspcSearcher.close
  }

  // for local (in-process) use from Scala
  def factory = new Factory {
    val cpc = new CPCService
    val ipc = new IPCService
    val uspc = new USPCService

    override def close = PatClasService.close
  }

  // for local (in-process) use from Java
  import org.t3as.patClas.api.javaApi.{Factory => JF}

  def toJavaApi = new JF(factory)
}

@Api
@Path("/v1.0/CPC")
class CPCService extends SearchService[CPCHit] with LookupService[CPCDescription] {
  val svc = PatClasService.service // singleton for things that must be shared across multiple instances; must be initialised first
  import svc._

  log.debug("CPCService:ctor")

  @Path("search")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def search(@QueryParam("q") q: String, @QueryParam("stem") stem: Boolean = true, @QueryParam("symbol") symbol: String = null) = cpcSearcher.search(q, stem, Option(symbol))

  @Path("suggest")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def suggest(@QueryParam("prefix") prefix: String, @QueryParam("num") num: Int) = cpcSuggest(prefix, num)


  // FIX BW 19/11/2016 Probably doesn't belong here, test
  def ensureSubgroup(symbol: String): String = {
    // CPC symbol made up of:
    //      section: A-H,Y 
    //      class: 2 digit number
    //      subclass: A-Z
    //      maingroup: 1 to 9999, can be blank if classification in subclasses only
    //      subgroup: 00 to 999999, can be blank if classification in subclasses only

    // Add /00 to maingroup if present
    // B42F1 =>  B42F1/00
    // if maingroup present without subgroup add /00 subgroup
    "^[A-HY]\\d{2}[A-Z]\\d{1,4}$".r.findFirstIn(symbol).fold(symbol)(_ => symbol + "/00")
  }


  @Path("ancestorsAndSelf")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def ancestorsAndSelf(@QueryParam("symbol") symbol: String, @QueryParam("format") format: String) = {
    val fmt = getToText(format)
    database withSession { implicit session =>
      cpcDb.getSymbolWithAncestors(ensureSubgroup(symbol.trim)).map(_.toDescription(fmt))
    }
  }

  @Path("bulkAncestorsAndSelf")
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def bulkAncestorsAndSelf(bulkSymbolLookup: BulkSymbolLookup) = {
    val fmt = getToText(bulkSymbolLookup.format)
    // FIX BW 16/11/2016 get rid of this mutable
    val results: collection.mutable.HashMap[String, List[CPCDescription]] = collection.mutable.HashMap()
    database withSession { implicit session =>
      bulkSymbolLookup.symbols.foreach(symbol => {
        results.put(symbol, cpcDb.getSymbolWithAncestors(ensureSubgroup(symbol.trim)).map(_.toDescription(fmt)))
      })
    }
    results.toMap
  }

  @Path("children")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def children(@QueryParam("parentId") parentId: Int, @QueryParam("format") format: String,
                        @QueryParam("grandchildCounts") grandchildCounts: Boolean) = {
    val fmt = getToText(format)
    database withSession { implicit session =>
      if (grandchildCounts) cpcDb.getChildrenWithGrandchildCounts(parentId).map(c => c._1.toDescription(fmt, c._2))
      else cpcDb.getChildren(parentId).map(_.toDescription(fmt))
    }
  }
}

@Api
@Path("/v1.0/IPC")
class IPCService extends SearchService[IPCHit] with LookupService[IPCDescription] {
  val svc = PatClasService.service

  import svc._

  log.debug("IPCService:ctor")

  @Path("search")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def search(@QueryParam("q") q: String, @QueryParam("stem") stem: Boolean = true, @QueryParam("symbol") symbol: String = null) = ipcSearcher.search(q, stem, Option(symbol))

  @Path("suggest")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def suggest(@QueryParam("prefix") prefix: String, @QueryParam("num") num: Int) = ipcSuggest(prefix, num)

  @Path("ancestorsAndSelf")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def ancestorsAndSelf(@QueryParam("symbol") symbol: String, @QueryParam("format") format: String) = {
    val fmt = getToText(format)
    database withSession { implicit session =>
      ipcDb.getSymbolWithAncestors(symbol.trim).map(_.toDescription(fmt))
    }
  }

  @Path("bulkAncestorsAndSelf")
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def bulkAncestorsAndSelf(bulkSymbolLookup: BulkSymbolLookup) = {
    val fmt = getToText(bulkSymbolLookup.format)
    // FIX BW 16/11/2016 get rid of this mutable
    val results: collection.mutable.HashMap[String, List[IPCDescription]] = collection.mutable.HashMap()
    database withSession { implicit session =>
      bulkSymbolLookup.symbols.foreach(symbol => {
        results.put(symbol, ipcDb.getSymbolWithAncestors(symbol.trim).map(_.toDescription(fmt)))
      })
    }
    results.toMap
  }

  @Path("children")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def children(@QueryParam("parentId") parentId: Int, @QueryParam("format") format: String,
                        @QueryParam("grandchildCounts") grandchildCounts: Boolean) = {
    val fmt = getToText(format)
    database withSession { implicit session =>
      if (grandchildCounts) ipcDb.getChildrenWithGrandchildCounts(parentId).map(c => c._1.toDescription(fmt, c._2))
      else ipcDb.getChildren(parentId).map(_.toDescription(fmt))
    }
  }
}

@Api
@Path("/v1.0/USPC")
class USPCService extends SearchService[USPCHit] with LookupService[USPCDescription] {
  val svc = PatClasService.service

  import svc._

  log.debug("USPCService:ctor")

  @Path("search")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def search(@QueryParam("q") q: String, @QueryParam("stem") stem: Boolean = true, @QueryParam("symbol") symbol: String = null) = uspcSearcher.search(q, stem, Option(symbol))

  @Path("suggest")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def suggest(@QueryParam("prefix") prefix: String, @QueryParam("num") num: Int) = uspcSuggest(prefix, num)

  @Path("ancestorsAndSelf")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def ancestorsAndSelf(@QueryParam("symbol") symbol: String, @QueryParam("format") format: String) = {
    val fmt = getToText(format)
    database withSession { implicit session =>
      uspcDb.getSymbolWithAncestors(symbol.trim).map(_.toDescription(fmt))
    }
  }

  @Path("bulkAncestorsAndSelf")
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def bulkAncestorsAndSelf(bulkSymbolLookup: BulkSymbolLookup) = {
    val fmt = getToText(bulkSymbolLookup.format)
    // FIX BW 16/11/2016 get rid of this mutable
    val results: collection.mutable.HashMap[String, List[USPCDescription]] = collection.mutable.HashMap()
    database withSession { implicit session =>
      bulkSymbolLookup.symbols.foreach(symbol => {
        results.put(symbol, uspcDb.getSymbolWithAncestors(symbol.trim).map(_.toDescription(fmt)))
      })
    }
    results.toMap
  }

  @Path("children")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  override def children(@QueryParam("parentId") parentId: Int, @QueryParam("format") format: String,
                        @QueryParam("grandchildCounts") grandchildCounts: Boolean) = {
    val fmt = getToText(format)
    database withSession { implicit session =>
      if (grandchildCounts) uspcDb.getChildrenWithGrandchildCounts(parentId).map(c => c._1.toDescription(fmt, c._2))
      else uspcDb.getChildren(parentId).map(_.toDescription(fmt))
    }
  }
}

