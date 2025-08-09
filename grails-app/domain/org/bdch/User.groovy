package org.bdch

class User {

   Long id
   Long version // No idea what this field is, lets map it I guess ...
   String username
   String passwordHash

   static hasMany = [session: Session]
   static hasOne = [spotifyUser: SpotifyUser]

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
