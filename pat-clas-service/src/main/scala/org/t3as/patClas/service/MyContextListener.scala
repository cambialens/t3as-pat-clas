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

package org.t3as.patClas.service

import javax.servlet.{ServletContextEvent, ServletContextListener}

class MyContextListener extends ServletContextListener {

  // FIX BW 17/11/2016 Hook the SwaggerBootstrap in here rather than in BootstrapServlet?
  override def contextInitialized(event: ServletContextEvent) = PatClasService.init
  
  override def contextDestroyed(event: ServletContextEvent) = PatClasService.close
}
