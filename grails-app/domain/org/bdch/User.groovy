package org.bdch

import grails.gorm.annotation.Entity

@Entity
class User {

   Long id
   Long version // No idea what this field is, lets map it I guess ...
   String username
   String passwordHash

   static hasOne = [session: Session]

   static mapping = {
      table 'user_account'
      username column: 'user_name'
      passwordHash column: 'password_crypt'
   }

   static constraints = {
      username blank: false, unique: true
      passwordHash blank: false
   }
}
