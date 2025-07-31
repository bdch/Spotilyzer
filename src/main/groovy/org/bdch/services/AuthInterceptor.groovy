package org.bdch.services

import grails.converters.JSON
import org.bdch.User

class AuthInterceptor {

   AuthService authService

   AuthInterceptor() {
      matchAll()
      // Exclude login and register actions from authentication
      exclude(controller: "auth", action: "login")
      exclude(controller: "auth", action: "register")
   }

   boolean before() {
      String sessionKey = request.getHeader("X-Session-Key")
      if (!sessionKey) {
         response.status = 401 // Unauthorized
         render([status: "error", message: "Session key is required"] as JSON)
         return false
      }
      User user = authService.findUserBySessionKey(sessionKey)

      if (!user) {
         render status: 401, text: 'Unauthorized: Invalid session key'
         return false
      }
      request.user = user
      return true
   }
}
