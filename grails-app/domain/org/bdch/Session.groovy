package org.bdch

import grails.gorm.annotation.Entity

class Session {

   Long id
   Long version
   String sessionKey
   User user
   Long creation_timestamp

   static belongsTo = [user: User]

   static mapping = {
      table 'user_session'
      sessionKey column: 'session_key', unique: true
      user column: 'user_id'
      creation_timestamp column: 'creation_timestamp'
   }

   static constraints = {
      sessionKey nullable: false, blank: false, unique: true
      user nullable: false
      creation_timestamp nullable: false
   }
}
