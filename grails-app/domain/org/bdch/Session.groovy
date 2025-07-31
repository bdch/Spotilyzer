package org.bdch

class Session {

   Long id
   Long version
   String sessionKey
   User owner
   Long cts // creation timestamp

   static belongsTo = [owner: User]

   static mapping = {
      table 'user_session'
      sessionKey column: 'session_key', unique: true
      owner column: 'owner'
      cts column: 'creation_timestamp'
   }

   static constraints = {
      sessionKey nullable: false, blank: false, unique: true
      owner nullable: false
      cts nullable: false
    }
}
