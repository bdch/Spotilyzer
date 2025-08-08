package util

import groovy.json.JsonBuilder
import org.bdch.services.AuthService
import org.eclipse.jetty.util.ajax.JSON
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SessionInterceptor implements HandlerInterceptor {

   Logger logger = LoggerFactory.getLogger(SessionInterceptor.class)
   AuthService authService

   @Override
   boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

      // Skip validation for login/register endpoints
      String uri = request.requestURI
      if (uri.contains('/') || uri.contains('/register') || uri.contains('/auth/renderLoginPage')) {
         return true
      }

      String sessionKey = request.getHeader('X-Session-Key') ?: request.getParameter('sessionKey')

      def validation = authService.validateSession(sessionKey)

      if (!validation.valid) {
         logger.warn("Invalid session access attempt: ${validation.message}")
         response.status = 401
         response.contentType = 'application/json'
         def jsonResponse = new JsonBuilder([status: 'error', message: validation.message])
         response.writer.write(jsonResponse.toString())
         return false
      }

      request.setAttribute('currentUser', validation.user)
      return true
   }
}
