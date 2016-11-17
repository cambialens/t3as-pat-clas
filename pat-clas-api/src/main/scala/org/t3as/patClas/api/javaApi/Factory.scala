/*
    Copyright 2014 NICTA
    
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

package org.t3as.patClas.api.javaApi

import java.io.Closeable
import java.util.{List => JList, Map => JMap}

import scala.collection.JavaConverters._
import org.t3as.patClas.api._
import org.t3as.patClas.api.API.{Factory => sFactory, LookupService => sLookupService, SearchService => sSearchService}

import scala.collection.mutable


/**
  * Results are implicity converted to Java objects
  * @param l
  * @tparam D
  */
class LookupAdapter[D](l: sLookupService[D]) {
  def ancestorsAndSelf(symbol: String, format: String): JList[D] = l.ancestorsAndSelf(symbol, format).asJava
  def bulkAncestorsAndSelf(symbols: JList[String], format: String): JMap[String, JList[D]] = {
    val a : mutable.ArraySeq[String] = mutable.ArraySeq() ++ symbols.asScala
    val map: Map[String, List[D]] = l.bulkAncestorsAndSelf(BulkSymbolLookup(a, format))
    map.mapValues(v => v.asJava).asJava
  }
  def children(parentId: Int, format: String): JList[D] = l.children(parentId, format).asJava
}

trait Suggestions {
  def getExact: JList[String]
  def getFuzzy: JList[String]
}

class SearchAdapter[H <: HitBase](s: sSearchService[H]) {
  def search(q: String, stem: Boolean = true, symbol: String = null): JList[H] = s.search(q, stem, symbol).asJava
  def suggest(prefix: String, num: Int): Suggestions = new Suggestions {
    val x = s.suggest(prefix, num)
    override def getExact = x.exact.asJava
    override def getFuzzy = x.fuzzy.asJava
  }
}

class Factory(f: sFactory) extends Closeable {
  
  def getCPCLookup = new LookupAdapter(f.cpc)
  def getIPCLookup = new LookupAdapter(f.ipc)
  def getUSPCLookup = new LookupAdapter(f.uspc)
  
  def getCPCSearch = new SearchAdapter[CPCHit](f.cpc)
  def getIPCSearch = new SearchAdapter[IPCHit](f.ipc)
  def getUSPCSearch = new SearchAdapter[USPCHit](f.uspc)
  
  def close = f.close
}