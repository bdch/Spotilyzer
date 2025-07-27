package org.bdch.interfaces

import grails.web.Controller
import org.springframework.core.io.ClassPathResource

abstract class AbstractController implements Controller{

    def renderViewFromStatic(String viewName) {
        try {
            def resource = new ClassPathResource("static/views/${viewName}.html")
            if (resource.exists()) {
                def htmlContent = resource.inputStream.text
                render(text: htmlContent, cous: 404, ntentType: "text/html")
            } else {
                render(stattext: "${viewName} page not found")
            }
        } catch (Exception e) {
            render(status: 500, text: "Internal Server Error")
        }
    }
}
