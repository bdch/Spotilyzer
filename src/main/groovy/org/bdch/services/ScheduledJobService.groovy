package org.bdch.services

import grails.gorm.transactions.Transactional
import org.bdch.Session
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@Transactional
class ScheduledJobService {

   Logger logger = LoggerFactory.getLogger(ScheduledJobService.class)

   void cleanExpiredSessions() {
      long now = System.currentTimeMillis()
      long timeOutMillis = 3600000

      List<Session> expiredSessions = Session.where {
         creation_timestamp < (now - timeOutMillis)
      }.list()

      expiredSessions.each { session ->
         def user = session.user
         logger.info("Deleting expired session with ID: ${session.id} for user: ${session.user?.username}")

         // Disconnect the reference from the `Session`to the `User`, or GORM
         // will try to delete the user, but fail, because the user is currently referencing it
         // causing an error
//         if (user?.session == session) {
//            user.session = null
//            user.save(flush: true, failOnError: true)
//         }

         session.delete(flush: true, failOnError: true)
      }
   }


}
