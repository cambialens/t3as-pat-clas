/*
    Copyright 2013 NICTA
    
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

package org.t3as.patClas.parse

import java.io.File
import java.util.zip.ZipFile
import scala.collection.JavaConversions.enumerationAsScalaIterator
import scala.slick.driver.H2Driver.simple.{columnBaseToInsertInvoker, ddlToDDLInvoker}
import scala.slick.driver.H2Driver.simple.Database
import scala.slick.driver.H2Driver.simple.Database.threadLocalSession
import scala.util.control.NonFatal
import scala.xml.XML
import org.slf4j.LoggerFactory
import org.t3as.patClas.api.T3asException
import org.t3as.patClas.common.CPCTypes.ClassificationItem
import org.t3as.patClas.common.IPCTypes.IPCEntry
import org.t3as.patClas.common.TreeNode
import org.t3as.patClas.common.USPCTypes.UsClass
import org.t3as.patClas.common.Util
import org.t3as.patClas.db.{CPCdb, IPCdb, USPCdb}
import org.t3as.patClas.search.index.IndexerFactory
import scala.slick.driver.ExtendedProfile
import scala.slick.lifted.DDL
import scala.slick.jdbc.meta.MTable



/** Load CPC into a database and search index.
  *
  * The input zip file is: http://www.cooperativepatentclassification.org/cpc/CPCSchemeXML201309.zip
  * (which is linked to from: http://www.cooperativepatentclassification.org/cpcSchemeAndDefinitions/Bulk.html,
  * which is probably a good place to look for updates). Manually download the zip file and pass its local path as args(0).
  */
object Load {

  val log = LoggerFactory.getLogger(getClass)

  case class Config(
    cpcZipFile: File = new File("CPCSchemeXML201309.zip"),
    ipcZipFile: File = new File("ipcr_scheme_20130101.zip"),
    uspcZipFile: File = new File("classdefsWith560fixed.zip"),
    dburl: String = "jdbc:h2:file:patClasDb",
    jdbcDriver: String = "org.h2.Driver",
    slickDriver: String = "scala.slick.driver.H2Driver",
    cpcIndexDir: File = new File("cpcIndex"),
    ipcIndexDir: File = new File("ipcIndex"),
    uspcIndexDir: File = new File("uspcIndex")
    )

  val parser = new scopt.OptionParser[Config]("load") {
    head("load", "0.x")
    val defValue = Config()

    opt[File]('c', "cpcZipFile") action { (x, c) =>
      c.copy(cpcZipFile = x)
    } text (s"path to CPC definitions in zipped XML, default ${defValue.cpcZipFile.getPath} (source http://www.cooperativepatentclassification.org/cpcSchemeAndDefinitions/Bulk.html)")
    opt[File]('d', "cpcIndexDir") action { (x, c) =>
      c.copy(cpcIndexDir = x)
    } text (s"path to CPC search index dir, default ${defValue.cpcIndexDir.getPath} (need not pre-exist)")

    opt[File]('i', "ipcZipFile") action { (x, c) =>
      c.copy(ipcZipFile = x)
    } text (s"path to IPC definitions in zipped XML, default ${defValue.ipcZipFile.getPath} (source http://www.wipo.int/classifications/ipc/en/ITsupport/Version20130101/index.html)")
    opt[File]('j', "ipcIndexDir") action { (x, c) =>
      c.copy(ipcIndexDir = x)
    } text (s"path to IPC search index dir, default ${defValue.ipcIndexDir.getPath} (need not pre-exist)")

    opt[File]('u', "uspcZipFile") action { (x, c) =>
      c.copy(uspcZipFile = x)
    } text (s"path to USPC definitions in zipped XML, default ${defValue.uspcZipFile.getPath} (source https://eipweb.uspto.gov/2013/ClassDefinitions)")
    opt[File]('v', "uspcIndexDir") action { (x, c) =>
      c.copy(ipcIndexDir = x)
    } text (s"path to IPC search index dir, default ${defValue.uspcIndexDir.getPath} (need not pre-exist)")

    opt[String]("dburl") action { (x, c) =>
      c.copy(dburl = x)
    } text (s"database url, default ${defValue.dburl}")
    
    opt[String]("jdbcDriver") action { (x, c) =>
      c.copy(jdbcDriver = x)
    } text (s"JDBC driver, default ${defValue.jdbcDriver}")

    opt[String]("slickDriver") action { (x, c) =>
      c.copy(slickDriver = x)
    } text (s"Slick database driver, default ${defValue.slickDriver}")

    help("help") text ("prints this usage text")
  }

  def main(args: Array[String]): Unit = {
    parser.parse(args, Config()) map { c =>
      Database.forURL(c.dburl, driver = c.jdbcDriver) withSession {
        DDL(Seq("CREATE USER IF NOT EXISTS READONLY PASSWORD ''"), Nil).create
        doCPC(c);
        doIPC(c);
        doUSPC(c);
      }
    }
    log.info(" ... done.")
  }

  def doCPC(c: Config) {
    if (c.cpcZipFile.exists()) {
      log.info(s"Parsing ${c.cpcZipFile} ...")
      val indexer = IndexerFactory.getCPCIndexer(c.cpcIndexDir)
      try {
        val dao = new CPCdb(Util.getObject(c.slickDriver))
        import dao.CPC

        // Create the table(s), indices etc.
        if (!MTable.getTables("cpc").list.isEmpty) CPC.ddl.drop
        CPC.ddl.create
        DDL(Seq("""GRANT SELECT ON "cpc" TO READONLY"""), Nil).create

        // An item for top level ClassificationItems to refer to as their "parent" (to satisfy the foreign key constraint)
        // Specifying the primary key (which is an autoInc column) works with H2, but won't work with mysql and some other databases.
        // With these databases some other means will be required to insert this row.
        CPC insert new ClassificationItem(Some(CPCdb.topLevel), CPCdb.topLevel, false, false, false, "2013-01-01", 0, "parent", "<text>none</text>", "<text>none</text>")

        def process(t: TreeNode[ClassificationItem], parentId: Int) = {
          dao.insertTree(t, parentId) // insert tree of ClassificationItems into db
          indexer.addTree(t) // add to search index
        }

        val zipFile = new ZipFile(c.cpcZipFile)
        try {
          // load section zip entries
          val sectionFileNameRE = """^scheme-[A-Z].xml$""".r
          zipFile.entries filter (e => sectionFileNameRE.findFirstIn(e.getName).isDefined) foreach { e =>
            CPCParser.parse(XML.load(zipFile.getInputStream(e))) foreach (process(_, CPCdb.topLevel))
          }

          // load subclass zip entries
          val subclassFileNameRE = """^scheme-[A-Z]\d\d[A-Z].xml$""".r
          zipFile.entries filter (e => subclassFileNameRE.findFirstIn(e.getName).isDefined) foreach { e =>
            CPCParser.parse(XML.load(zipFile.getInputStream(e))).foreach { n =>
              // each root node should be a level 5 ClassificationItem and should have already been added to the db from a section zip entry
              val c = n.value
              if (c.level != 5) throw new T3asException(s"Subclass file root node not level 5: ${c}")
              val parent = dao.compiled.getBySymbolLevel(c.symbol, c.level).firstOption.getOrElse(throw new T3asException(s"Subclass file root node not in db: ${c}"))
              n.children foreach (process(_, parent.id.getOrElse(throw new T3asException(s"Missing id from db record: ${parent}"))))
            }
          }
        } finally zipFile.close
      } finally indexer.close

    } else log.info(s"File ${c.cpcZipFile} not found, so skipping CPC load")
  }

  def doIPC(c: Config) {
    if (c.ipcZipFile.exists()) {
      log.info(s"Parsing ${c.ipcZipFile} ...")
      val indexer = IndexerFactory.getIPCIndexer(c.ipcIndexDir)
      try {
        val dao = new IPCdb(Util.getObject(c.slickDriver))
        import dao.IPC

        if (!MTable.getTables("ipc").list.isEmpty) IPC.ddl.drop
        IPC.ddl.create
        DDL(Seq("""GRANT SELECT ON "ipc" TO READONLY"""), Nil).create

        IPC insert new IPCEntry(Some(IPCdb.topLevel), IPCdb.topLevel, 0, "", "symbol", None, "<text>none</text>")

        def process(t: TreeNode[IPCEntry], parentId: Int) = {
          dao.insertTree(t, parentId) // insert tree of IPCEntry into db
          indexer.addTree(t) // add to search index
        }

        val zipFile = new ZipFile(c.ipcZipFile)
        try {
          zipFile.entries foreach { e =>
            // parent for English IPCEntries (skipping French for now)
            val parent = (XML.load(zipFile.getInputStream(e)) \ "revisionPeriod" \ "ipcEdition" \ "en" \ "staticIpc")(0)
            IPCParser.parse(parent) foreach (process(_, IPCdb.topLevel))
          }
        } finally zipFile.close

      } finally indexer.close

    } else log.info(s"File ${c.ipcZipFile} not found, so skipping IPC load")
  }

  /**
   * The US data is dirty and can't all be successfully processed, so we need to minimize the impact by
   * catching and logging errors and carrying on.
   */
  def doUSPC(c: Config) {
    if (c.uspcZipFile.exists()) {
      log.info(s"Parsing ${c.uspcZipFile} ...")
      val indexer = IndexerFactory.getUSPCIndexer(c.uspcIndexDir)
      try {
        val dao = new USPCdb(Util.getObject(c.slickDriver))
        import dao.USPC

        if (!MTable.getTables("uspc").list.isEmpty) USPC.ddl.drop
        USPC.ddl.create
        DDL(Seq("""GRANT SELECT ON "uspc" TO READONLY"""), Nil).create
        
        USPC insert new UsClass(Some(USPCdb.topId), USPCdb.topXmlId, USPCdb.topXmlId, "symbol", None, None, None, "<text>none</text>")

        def process(c: UsClass) = {
          try {
            USPC.forInsert insert c // insert UsClass into db
            indexer.add(c) // add to search index
          } catch {
            case NonFatal(e) => log.error("Can't load: " + c, e)
          }
        }

        // XML is not well formed. E.g. parsing classdefs201308/class_106.xml without using tagsoup, we get:
        // org.xml.sax.SAXParseException; lineNumber: 1645; columnNumber: 3; The element type "graphic" must be terminated by the matching end-tag "</graphic>".
        val saxp = new org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl().newSAXParser()

        val zipFile = new ZipFile(c.uspcZipFile)
        try {
          zipFile.entries filter (_.getName.endsWith(".xml")) foreach { ze =>
            log.info(s"Processing ${ze.getName}...")
            try {
              val root = XML.withSAXParser(saxp).load(zipFile.getInputStream(ze))
              log.debug("root.label = " + root.label) // tagsoup wraps our top level class element in html/body
              val usClass = if (root.label == "html") (root \ "body" \ "class")(0) else root
              USPCParser.parse(usClass, process)
            } catch {
              case NonFatal(e) => log.error("Can't process zip entry: " + ze.getName, e)
            }
          }
        } finally zipFile.close

      } finally indexer.close

    } else log.info(s"File ${c.uspcZipFile} not found, so skipping USPC load")
  }

}