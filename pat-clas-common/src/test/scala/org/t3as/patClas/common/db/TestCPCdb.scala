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

package org.t3as.patClas.common.db

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConversions._
import scala.slick.driver.H2Driver
import scala.slick.jdbc.JdbcBackend.{Database, Session}
import org.slf4j.LoggerFactory
import org.t3as.patClas.common.TreeNode
import org.t3as.patClas.common.CPCUtil.ClassificationItem

import scala.slick.jdbc.JdbcBackend

class TestCPCdb extends FlatSpec with Matchers {
  val log = LoggerFactory.getLogger(getClass)
  val dao = new CPCdb(H2Driver)

  def withDatabase(test: (JdbcBackend.Session) => Any) {
    // Connect to the database and execute the following block within a session
    Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver") withSession { implicit session =>

      import dao.profile.simple._
      import dao._

      // Create the table(s), indices etc.
      cpcs.ddl.create

      // an item for top level ClassificationItems to refer to as their "parent"; forceInsert overrides the autoInc id, may not work on all databases
      val id = cpcs forceInsert ClassificationItem(Some(CPCdb.topLevel), CPCdb.topLevel, false, false, false, "2013-01-01", 0, "parent", "universal ancestor", "no notes")

      val l8 = TreeNode(ClassificationItem(None, -1, false, true, false, "2013-01-01", 8, "B29C31/002", "title8", "notes8"), Seq())
      val l7 = TreeNode(ClassificationItem(None, -1, false, true, false, "2013-01-01", 7, "B29C31/00",  "title7", "notes7"), Seq(l8))
      val l6 = TreeNode(ClassificationItem(None, -1, false, true, false, "2013-01-01", 6, "B29C31/00",  "title6", "notes6"), Seq(l7))
      val l5 = TreeNode(ClassificationItem(None, -1, false, false, false, "2013-01-01", 5, "B29C",      "title5", "notes5"), Seq(l6))
      dao.insertTree(l5, CPCdb.topLevel)

      test(session)
    }
  }

  "CPCdb" should "load and lookup" in withDatabase { implicit session =>
    import dao.profile.simple._

    a [java.sql.SQLException] should be thrownBy {
        // duplicate (symbol, level) value and there is a unique index
        dao.compiled.insert += ClassificationItem(None, CPCdb.topLevel, false, false, false, "2013-01-01", 5, "B29C", "title5", "notes5")
    }

    val list = dao.compiled.getBySymbol("B29C31/00").list
    list.size should be(1)
    list.exists(c => c.symbol == "B29C31/00" && c.level == 7) should be(true)

    val l7list = dao.compiled.getBySymbolLevel("B29C31/00", 7).list
    l7list.size should be(1)
    l7list.exists(c => c.symbol == "B29C31/00" && c.level == 7) should be(true)

    dao.getSymbolWithAncestors("B29C31/00") zip Seq((1, 0, 5, "B29C"), (2, 1, 7, "B29C31/00")) foreach {
      case (c, (id, parentId, level, symbol)) => {
        c.id should be (Some(id))         // autoInc id starts at 1
        c.parentId should be (parentId)   // insertTree fixes the parentId (-1 above is ignored)
        c.level should be (level)
        c.symbol should be (symbol)
      }
    }
  }

  "CPCdb.getChildrenWithGrandchildCounts()" should "return the counts of grandchildren" in withDatabase { implicit session =>
    val subclassId = dao.getSymbol("B29C").get.id.get
    val groups = dao.getChildrenWithGrandchildCounts(subclassId)

    groups.size should be(1)
    groups(0)._1.symbol should be("B29C31/00")
    groups(0)._2 should be(1)

    val children = dao.getChildrenWithGrandchildCounts(groups(0)._1.id.get)

    children.size should be(1)
    children(0)._1.symbol should be("B29C31/002")
    children(0)._2 should be(0)
  }

  it should "not include top level when getting children of top level" in withDatabase { implicit session =>
    val groups = dao.getChildrenWithGrandchildCounts(CPCdb.topLevel)

    groups.size should be(1)
  }
}
