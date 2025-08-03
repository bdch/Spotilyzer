package org.bdch.jobs

import org.bdch.services.ScheduledJobService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.time.Instant

@Component
class SessionCleanupJob {

   Logger logger = LoggerFactory.getLogger(SessionCleanupJob.class)

   @Autowired
   ScheduledJobService scheduledJobService

   @Scheduled(fixedRate = 3600000L) // Every hour
   def execute() {
      try {
         scheduledJobService.cleanExpiredSessions()
         logger.info("org.bdch.Session cleanup job executed successfully at ${Instant.now()}.")
      } catch (Exception e) {
         logger.error("Error during session cleanup job execution: ${e.message}", e)
      }
   }
}
