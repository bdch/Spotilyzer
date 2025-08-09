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

   // /auth/spotify is NOT here - it needs normal cookie validation
   // /auth/callback is NOT here - it has special state parameter validation
   //  The flow is:
   // 1. User clicks "Login with Spotify" with the initial request to `/auth/spotify`
   // -> Uses normal cookie-based session validation
   // -> Sets currentUser attribute
   // -> AuthController can access the user and put sessionKey in state parameter
   // 2. Spotify redirects to `/auth/callback` with the state parameter
   // -> Uses state parameter for session validation
   // -> Sets currentUser attribute
   // -> AuthController can access the user to save Spotify data
   private static List<String> excludedEndpoints = [
      '/loginPage',
      '/auth/register',
      '/registerPage',
      '/error'
   ]

   // Pattern for static resources (CSS, JS, images, etc.)
   private static Pattern staticResourcePattern = ~/.*\.(css|js|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$/

   @Override
   boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
      logger.info("Checking session for URI: ${request.requestURI}")
      String uri = request.requestURI
      if (uri == '/auth/callback') {
         return handleSpotifyCallback(request, response)
      }

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

   // The session interceptor  runs and validates, but if `/auth/spotify` was excluded,
   // the interceptor will not set the `currentUser` attribute.
   // So we need to handle the Spotify callback separately.
   private boolean handleSpotifyCallback(HttpServletRequest request, HttpServletResponse response) {
      String sessionKey = request.getParameter('state')
      if (!sessionKey) {
         logger.warn("No state parameter in Spotify callback")
         return sendUnauthorized(response, 'Invalid callback - no state')
      }

      def validation = authService.validateSession(sessionKey)
      if (!validation.valid) {
         logger.warn("Invalid session in Spotify callback: ${validation.message}")
         return sendUnauthorized(response, validation.message)
      } else {
         logger.info("Session validated successfully for Spotify callback, user: ${validation.user.username}")
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
