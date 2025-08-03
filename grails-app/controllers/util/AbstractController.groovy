package util

import grails.artefact.Controller
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource

abstract class AbstractController implements Controller {

   Logger logger = LoggerFactory.getLogger(AbstractController.class)

   def renderViewFromStatic(String viewName) {
      try {
         logger.info("Trying to render view: $viewName")
         def resource = new ClassPathResource("static/views/${viewName}.html")
         if (resource.exists()) {
            def htmlContent = resource.inputStream.text
            render(text: htmlContent, cous: 404, contentType: "text/html")
         } else {
            render(stattext: "${viewName} page not found")
         }
      } catch (Exception e) {
         logger.info("Error rendering view: $viewName, Exception: $e")
         render(status: 500, text: "$e.message")
      }
   }
}
