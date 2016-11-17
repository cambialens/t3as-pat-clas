package org.t3as.patClas.swagger

import javax.servlet.ServletConfig
import javax.servlet.http.HttpServlet

import io.swagger.jaxrs.config.BeanConfig

class SwaggerBootstrap extends HttpServlet {

  override def init(config: ServletConfig) = {
    super.init(config)
    val beanConfig = new BeanConfig()
    beanConfig.setVersion("1.0")
    beanConfig.setSchemes(Array("http"))
    beanConfig.setHost("localhost:8080")
    beanConfig.setBasePath("/pat-clas-service/rest")
    beanConfig.setResourcePackage("io.swagger.resource,org.t3as.patClas.service")
    beanConfig.setScan(true)
  }
}
