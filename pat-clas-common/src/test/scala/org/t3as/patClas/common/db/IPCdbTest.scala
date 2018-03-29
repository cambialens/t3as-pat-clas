package org.t3as.patClas.common.db

import org.scalatest.{FlatSpec, Matchers}
import org.t3as.patClas.common.IPCUtil.IPCEntry
import org.t3as.patClas.common.TreeNode

import scala.slick.driver.H2Driver
import scala.slick.jdbc.JdbcBackend
import scala.slick.jdbc.JdbcBackend.Database

class IPCdbTest extends FlatSpec with Matchers {
  val dao = new IPCdb(H2Driver)

  def withDatabase(test: (JdbcBackend.Session) => Any) {
    // Connect to the database and execute the following block within a session
    Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver") withSession { implicit session =>

      import dao.profile.simple._
      import dao._

      // Create the table(s), indices etc.
      ipcs.ddl.create

      // an item for top level IPCEntries to refer to as their "parent"; forceInsert overrides the autoInc id, may not work on all databases
      val id = ipcs forceInsert IPCEntry(Some(IPCdb.topLevel), IPCdb.topLevel, 0, "", "universal ancestor", None, "<text>none</text>")

      val l8 = TreeNode(IPCEntry(None, -1, 8, "1", "A01B", None, "notes8"), Seq())
      val l7 = TreeNode(IPCEntry(None, -1, 7, "c", "A01", None, "notes7"), Seq(l8))
      val l6 = TreeNode(IPCEntry(None, -1, 6, "t", "A01", Some("A01"), "notes6"), Seq())
      val l5 = TreeNode(IPCEntry(None, -1, 5, "s", "A",      None, "notes5"), Seq(l6, l7))
      dao.insertTree(l5, IPCdb.topLevel)

      test(session)
    }
  }

  "IPCdb.getChildrenWithGrandchildCounts()" should "return the count of subgroups" in withDatabase { implicit session =>
    val subclassId = dao.getSymbol("A").get.id.get
    val groups = dao.getChildrenWithGrandchildCounts(subclassId)

    groups.size should be(2)
    groups.map(_._1.symbol) should contain theSameElementsAs List("A01", "A01")
    groups.map(_._2) should contain allOf (1, 0)

    val children = dao.getChildrenWithGrandchildCounts(groups.filter(_._2 > 0)(0)._1.id.get)

    children.size should be(1)
    children(0)._1.symbol should be("A01B")
    children(0)._2 should be(0)
  }

  it should "not include top level when getting children of top level" in withDatabase { implicit session =>
    val groups = dao.getChildrenWithGrandchildCounts(CPCdb.topLevel)

    groups.size should be(1)
  }
}
