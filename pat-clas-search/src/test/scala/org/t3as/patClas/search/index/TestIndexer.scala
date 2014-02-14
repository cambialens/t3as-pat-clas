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

package org.t3as.patClas.search.index

import org.apache.lucene.index.{DirectoryReader, Term}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, TermQuery}
import org.apache.lucene.store.RAMDirectory
import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory

import org.t3as.patClas.common.{TreeNode, Util}
import org.t3as.patClas.common.CPCTypes.ClassificationItem
import org.t3as.patClas.common.CPCTypes.IndexFieldName._
import org.t3as.patClas.search.Constants

class TestIndexer extends FlatSpec with Matchers {
  val log = LoggerFactory.getLogger(getClass)

  val xml = """<class-title date-revised="2013-01-01">
    			<title-part>
    				<text scheme="ipc">SHAPING OR JOINING OF PLASTICS</text>
    			</title-part>
    			<title-part><text scheme="ipc">SHAPING OF SUBSTANCES IN A PLASTIC STATE, IN GENERAL</text></title-part>
    			<title-part>
    				<text scheme="ipc">AFTER-TREATMENT OF THE SHAPED PRODUCTS, e.g. REPAIRING</text>
    				<reference>
    					<CPC-specific-text>
    						<text scheme="cpc"> moulding devices for producing toilet or cosmetic sticks
    							<class-ref scheme="cpc">A45D40/16</class-ref>
    						</text>
    					</CPC-specific-text>
    					<text scheme="ipc"> ; working in the manner of metal
    						<class-ref scheme="cpc">B23</class-ref>; grinding, polishing 
    						<class-ref scheme="cpc">B24</class-ref>; cutting 
    						<class-ref scheme="cpc">B26D</class-ref>, 
    						<class-ref scheme="cpc">B26F</class-ref>; making preforms 
    						<class-ref scheme="cpc">B29B11/00</class-ref> ; making laminated products by combining previously unconnected layers which become one product whose layers will remain together 
    						<class-ref scheme="cpc">B32B37/00</class-ref> - 
    						<class-ref scheme="cpc">B32B41/00</class-ref>
    					</text>
    				</reference>
    			</title-part>
    		</class-title>"""

  "xmlToText" should "concanenate text node content " in {
      
    val text = Util.toText(xml)
    log.debug(s"text = $text")

    val expected = """SHAPING OR JOINING OF PLASTICS
SHAPING OF SUBSTANCES IN A PLASTIC STATE, IN GENERAL
AFTER-TREATMENT OF THE SHAPED PRODUCTS, e.g. REPAIRING
moulding devices for producing toilet or cosmetic sticks
A45D40/16
; working in the manner of metal
B23
; grinding, polishing
B24
; cutting
B26D
,
B26F
; making preforms
B29B11/00
; making laminated products by combining previously unconnected layers which become one product whose layers will remain together
B32B37/00
-
B32B41/00"""
    text should be (expected)
  }

  "Indexer" should "make searchable index" in {
    val dir = new RAMDirectory
    val indexer = IndexerFactory.getCPCTestIndexer(dir)
    try {
      
      val title8 = xml
        
      val notes = """<notes-and-warnings date-revised="2013-01-01"><note type="note"><note-paragraph><pre><br/>
						1. Attention is drawn to Note (3) following the title of class 
    					<class-ref scheme="cpc">B29</class-ref>.
						<br/><br/>
						2. In this subclass:
						<br/>
						- repairing of articles made from plastics or substances in
						<br/>
						a plastic state, e.g. of articles shaped or produced by
						<br/>
						using techniques covered by this subclass or subclass <class-ref scheme="cpc">B29D</class-ref>,
						<br/>
						is classified in group
						<class-ref scheme="cpc">B29C73/00</class-ref>
						;
						<br/>
						- component parts, details, accessories or auxiliary
						<br/>
						operations which are applicable to more than one moulding
						<br/>
						technique a reclassified in groups
						<class-ref scheme="cpc">B29C31/00</class-ref>
						to
						<class-ref scheme="cpc">B29C37/00</class-ref>
						;
						<br/>
						- component parts, details, accessories or auxiliary
						<br/>
						operations which are only of use for one specific shaping
						<br/>
						technique a reclassified only in the relevant subgroups of
						<br/>
						groups
						<class-ref scheme="cpc">B29C39/00</class-ref>
						to
						<class-ref scheme="cpc">B29C71/00</class-ref>
						.
						<br/></pre><br/></note-paragraph></note>
    		</notes-and-warnings>"""
        
      val level8 = TreeNode(ClassificationItem(None, -1, false, true,  false, "2013-01-01", 8, "B29C31/002", title8, notes), Seq())
      
      val title7 = """<class-title date-revised="2013-01-01">
    			<title-part>
    				<text scheme="ipc">faking text for title7</text>
    			</title-part>
    		</class-title>"""
      val level7 = TreeNode(ClassificationItem(None, -1, false, true,  false, "2013-01-01", 7, "B29C31/00",  title7, notes), Seq(level8))
      
      val title6 = """<class-title date-revised="2013-01-01">
    			<title-part>
    				<text scheme="ipc">faking text for title6</text>
    			</title-part>
    		</class-title>"""
      val level6 = TreeNode(ClassificationItem(None, -1, false, true,  false, "2013-01-01", 6, "B29C31/00",  title6, notes), Seq(level7))
      
      val title5 = """<class-title date-revised="2013-01-01">
    			<title-part>
    				<text scheme="ipc">faking text for title5v</text>
    			</title-part>
    		</class-title>"""
      val level5 = TreeNode(ClassificationItem(None, -1, false, false, false, "2013-01-01", 5, "B29C",       title5, notes), Seq(level6))
      
      indexer.addTree(level5)
    } finally {
      indexer.close
    }
    
    val searcher = new IndexSearcher(DirectoryReader.open(dir))
    searcher.getIndexReader.numDocs() should be (4)
    try {
      {
        val topDocs = searcher.search(new TermQuery(new Term(Symbol, "B29C31/00")), 10)
        topDocs.totalHits should be (2)
      }
      {
        val qp = new QueryParser(Constants.version, ClassTitle, Constants.analyzer)
        val q = qp.parse("FAKED")
        log.debug("q = {}", q)
        val topDocs = searcher.search(q, 10) // analyzer should make this match "faking"
        topDocs.totalHits should be (3)
      }
      {
        val qp = new QueryParser(Constants.version, NotesAndWarnings, Constants.analyzer)
        val q = qp.parse("Attention")
        log.debug("q = {}", q)
        val topDocs = searcher.search(q, 10)
        topDocs.totalHits should be (4)
      }
    } finally {
      searcher.getIndexReader.close
    }
  }

}
