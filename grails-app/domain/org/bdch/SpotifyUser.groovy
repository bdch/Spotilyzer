package org.bdch

import java.sql.Timestamp

/**
 * This class can be seen as an anchor, to link a Spotify user to the application user.
 * The main purpose should then not be to store the Spotify user data,
 * but to store the access and refresh tokens, as well as the token expiration.
*/
class SpotifyUser {

   Long id
   String spotifyId
   String displayName
   String accessToken
   String refreshToken
   Timestamp tokenExpiration

   String profileImageUrl
   String profileImageUrlSmall // for thumbnails

   static belongsTo = [user: User]
   static hasMany = [topTracks: SpotifyTopTrack, tracks: SpotifyTrack]

   static mapping = {
      table 'user_spotify'
      id column: 'id'
      spotifyId column: 'spotify_id', unique: true
      displayName column: 'display_name'
      accessToken column: 'access_token', type: 'text'
      refreshToken column: 'refresh_token', type: 'text'
      tokenExpiration column: 'token_expiration'
      profileImageUrl column: 'profile_image_url', type: 'text'
      profileImageUrlSmall column: 'profile_image_url_small', type: 'text'
      userId column: 'user_id'
   }

   static constraints = {
      spotifyId nullable: false, blank: false, unique: true
      displayName nullable: true, blank: true
      accessToken nullable: false, blank: false
      refreshToken nullable: false, blank: false
      tokenExpiration nullable: false
      user nullable: false
      profileImageUrl nullable: true
      profileImageUrlSmall nullable: true
   }
}
