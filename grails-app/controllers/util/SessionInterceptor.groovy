package util

import groovy.json.JsonBuilder
import org.bdch.services.AuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import javax.servlet.http.Cookie
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.regex.Pattern

@Component
class SessionInterceptor implements HandlerInterceptor {

   Logger logger = LoggerFactory.getLogger(SessionInterceptor.class)

   @Autowired
   AuthService authService

   private static List<String> excludedEndpoints = [
      '/loginPage',
      '/auth/register',
      '/registerPage',
      '/auth/callback',
      '/auth/spotify',
      '/error'
   ]

   // Pattern for static resources (CSS, JS, images, etc.)
   private static Pattern staticResourcePattern = ~/.*\.(css|js|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$/

   @Override
   boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
      logger.info("Checking session for URI: ${request.requestURI}")

      // Skip validation for login/register endpoints
      String uri = request.requestURI
      if (uri in excludedEndpoints || uri == '/') {
         return true
      }

      // Skip validation for static resources
      if (staticResourcePattern.matcher(uri).matches()) {
         logger.debug("Skipping session validation for static resource: $uri")
         return true
      }

      // Get sessionKey from cookie (or header/param as fallback)
      String sessionKey = getCookieValue(request, 'sessionKey') ?:
         request.getHeader('X-Session-Key') ?:
            request.getParameter('sessionKey')
      logger.info("Session key found: ${sessionKey ? 'yes' : 'no'}")

      if (!sessionKey) {
         logger.warn("No session key provided for URI: $uri")
         return sendUnauthorized(response, 'Session key required')
      }

      def validation = authService.validateSession(sessionKey)

      if (!validation.valid) {
         logger.warn("Invalid session access attempt for URI: $uri, reason: ${validation.message}")
         clearSessionCookie(response)
         return sendUnauthorized(response, validation.message)
      } else {
         logger.info("Session validated successfully for user: ${validation.user.username}")
         request.setAttribute('currentUser', validation.user)
         return true
      }
   }

   private static String getCookieValue(HttpServletRequest request, String cookieName) {
      Cookie[] cookies = request.getCookies()
      if (cookies) {
         for (Cookie cookie : cookies) {
            if (cookie.name == cookieName) {
               return cookie.value
            }
         }
      }
      return null
   }

   private static void clearSessionCookie(HttpServletResponse response) {
      Cookie sessionCookie = new Cookie('sessionKey', '')
      sessionCookie.maxAge = 0
      sessionCookie.path = '/'
      response.addCookie(sessionCookie)
   }

   private static boolean sendUnauthorized(HttpServletResponse response, String message) {
      response.status = 401
      response.contentType = 'application/json'
      def jsonResponse = new JsonBuilder([status: 'error', message: message])
      response.writer.write(jsonResponse.toString())
      return false
   }
}
