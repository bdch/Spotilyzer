package org.bdch

import java.sql.Timestamp

class SpotifyUser {

   Long id
   String spotifyId
   String displayName
   String accessToken
   String refreshToken
   Timestamp tokenExpiration

   static belongsTo = [user: User]

   static mapping = {
      table 'user_spotify'
      id column: 'id'
      spotifyId column: 'spotify_id', unique: true
      displayName column: 'display_name'
      accessToken column: 'access_token'
      refreshToken column: 'refresh_token'
      tokenExpiration column: 'token_expiration'
      userId column: 'user_id'
   }

    static constraints = {
      id nullable: false, blank: false, unique: true
      spotifyId nullable: false, blank: false, unique: true
      displayName nullable: true, blank: true
      accessToken nullable: false, blank: false
      refreshToken nullable: false, blank: false
      tokenExpiration nullable: false, blank: false
      userId nullable: false, blank: false
    }
}
