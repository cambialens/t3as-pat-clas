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

package org.t3as.patClas.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.slf4j.LoggerFactory

object ScalaJacksonJsonProvider {
  val log = LoggerFactory.getLogger(getClass)
  
  val mapper = {
    val m = new ObjectMapper with ScalaObjectMapper
    m.registerModule(DefaultScalaModule)
    m
  }
}
import ScalaJacksonJsonProvider._

class ScalaJacksonJsonProvider extends JacksonJsonProvider(mapper) {
  log.debug("ScalaJacksonJsonProvider:ctor")
}
