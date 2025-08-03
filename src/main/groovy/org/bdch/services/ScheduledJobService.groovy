package org.bdch.services

import grails.gorm.transactions.Transactional
import org.bdch.Session
import org.slf4j.Logger
import org.slf4j.LoggerFactory


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
         logger.info("Deleting expired session with ID: ${session.id} for user: ${session.owner.username}")
         session.delete(flush: true)
      }
   }


}
