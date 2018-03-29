package org.t3as.patClas.common.db

import org.scalatest.{FlatSpec, Matchers}
import org.t3as.patClas.common.TreeNode
import org.t3as.patClas.common.USPCUtil.UsClass

import scala.slick.driver.H2Driver
import scala.slick.jdbc.JdbcBackend
import scala.slick.jdbc.JdbcBackend.Database

class USPCdbTest extends FlatSpec with Matchers {
  val dao = new USPCdb(H2Driver)

  def withDatabase(test: (JdbcBackend.Session) => Any) {
    // Connect to the database and execute the following block within a session
    Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver") withSession { implicit session =>

      import dao.profile.simple._
      import dao._

      // Create the table(s), indices etc.
      uspcs.ddl.create

      // an item for top level UsClasses to refer to as their "parent"; forceInsert overrides the autoInc id, may not work on all databases
      val id = uspcs forceInsert UsClass(Some(USPCdb.topId), USPCdb.topXmlId, USPCdb.topXmlId, "symbol", None, None, None, "<text>none</text>")

      uspcs += UsClass(None, "C100S000000", USPCdb.topXmlId, "100", Some("PRESSES"), None, None, "notes5")
      uspcs += UsClass(None, "C100S001000", "C100S000000", "100/1", None, Some("BINDING:"), Some("Methods and apparatus for use in disposing a flexible binder tightly and circumferentially closed around the material."), "notes6")
      uspcs += UsClass(None, "C100S002000", "C100S001000", "100/2", None, Some("Methods:"), Some("Methods"), "notes7")
      uspcs += UsClass(None, "C100S003000", "C100S002000", "100/3", None, Some("Compacting and Binding:"), Some("Methods which include subjecting the material to compression other than and in addition to that due to the tightness of the flexible binder around the material."), "notes8")

      test(session)
    }
  }

  "IPCdb.getChildrenWithGrandchildCounts()" should "return the count of subgroups" in withDatabase { implicit session =>
    val ancestors = dao.getSymbolWithAncestors("100/1")
    val groups = dao.getChildrenWithGrandchildCounts(ancestors.last.id.get)

    groups.size should be(1)
    groups(0)._1.symbol should be("100/2")
    groups(0)._2 should be(1)

    val children = dao.getChildrenWithGrandchildCounts(groups.filter(_._2 > 0)(0)._1.id.get)

    children.size should be(1)
    children(0)._1.symbol should be("100/3")
    children(0)._2 should be(0)
  }

  it should "not include top level when getting children of top level" in withDatabase { implicit session =>
    val groups = dao.getChildrenWithGrandchildCounts(CPCdb.topLevel)

    groups.size should be(1)
  }
}
