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

package org.t3as.patClas.common

import org.t3as.patClas.api.USPC
import org.t3as.patClas.api.Format

object USPCTypes {

  case class Description(id: Int, symbol: String, classTitle: String, subClassTitle: String, subClassDescription: String, text: String) extends USPC.Description

  case class Hit(score: Float, symbol: String, classTitleHighlights: String, subClassTitleHighlights: String, subClassDescriptionHighlights: String, textHighlights: String) extends USPC.Hit

  /** Names of USPC fields in the Lucene index. */
  object IndexFieldName extends Enumeration {
    type IndexFieldName = Value
    val ID, Symbol, ClassTitle, SubClassTitle, SubClassDescription, Text = Value

    implicit def convert(f: IndexFieldName) = f.toString
  }
  import IndexFieldName._
  
  val hitFields: Set[String] = Set(Symbol)
  
  def mkHit(score: Float, f: Map[String, String], h: Map[String, String]) = Hit(score, f(Symbol), h.getOrElse(ClassTitle, ""), h.getOrElse(SubClassTitle, ""), h.getOrElse(SubClassDescription, ""), h.getOrElse(Text, ""))

  /** Entity class mapping to a database row representing a USPC Symbol.
    */
  case class UsClass(id: Option[Int], xmlId: String, parentXmlId: String, symbol: String, classTitle: Option[String], subClassTitle: Option[String], subClassDescription: Option[String], text: String) {
    def toDescription(f: String => String) = Description(id.get, symbol, classTitle.getOrElse(""), subClassTitle.map(f).getOrElse(""), subClassDescription.map(f).getOrElse(""), f(text))
  }

}