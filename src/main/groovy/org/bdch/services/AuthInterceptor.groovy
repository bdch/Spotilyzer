package org.bdch.services

class AuthInterceptor {


   boolean before() {
      def sessionKey = request.getHeader("Authorization")?.replace("Session ", "")
      request.setAttribute("currentUser", Session.findBySessionKey(sessionKey)?.owner)
      return true
   }


}
