package org.t3as.patClas.swagger

import javax.servlet.ServletConfig
import javax.servlet.http.HttpServlet

import io.swagger.jaxrs.config.BeanConfig

class SwaggerBootstrap extends HttpServlet {

  override def init(config: ServletConfig) = {
    super.init(config)
    val beanConfig = new BeanConfig()
    beanConfig.setTitle("Patent Classification API")
    beanConfig.setVersion(config.getInitParameter("version"))
    beanConfig.setSchemes(Array("http", "https"))
    beanConfig.setHost(config.getInitParameter("host"))
    beanConfig.setBasePath("rest")
    beanConfig.setResourcePackage("io.swagger.resource,org.t3as.patClas.service")
    beanConfig.setScan(true)
  }
}
