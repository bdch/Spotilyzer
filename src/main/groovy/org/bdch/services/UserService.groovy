package org.bdch.services

import groovy.transform.CompileStatic
import org.bdch.Session
import org.bdch.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

@Transactional
class UserService {

   Logger logger = LoggerFactory.getLogger(UserService.class)

   // TODO change password

   def deleteUser(String username) {
      def userToDelete = User.findByUsername(username)
      if (!userToDelete) {
         logger.warn("User not found for deletion: ${username}")
         return false
      }
      // Delete sessions FIRST to avoid foreign key constraints
      Session.executeUpdate("delete from Session s where s.user = :user", [user: userToDelete])
      userToDelete.delete(flush: true, failOnError: true)
      logger.info("User ${username} deleted successfully")
      return true
   }

   def findUserForCurrentSession(String sessionKey) {
      def session = Session.findBySessionKey(sessionKey)
      if (!session) {
         logger.error("User not found for session key: ${sessionKey}")
         return null
      }
      logger.info("User found for session key: ${sessionKey}")
      return session.owner
   }


   // rename


}
