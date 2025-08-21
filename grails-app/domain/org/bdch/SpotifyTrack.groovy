package org.bdch

import java.sql.Timestamp

class SpotifyTrack {

   Long id
   String trackId
   String  trackName
   String imageUrl
   String spotifyUrl // Link to the track on Spotify
   Integer popularity
   Timestamp fetchedAt

   SpotifyArtist spotifyArtist
   static hasMany = [users: SpotifyUser]
   static belongsTo = [] // No cascading, since when a track is deleted, it should not delete the artist or users

   static mapping = {
      table 'spotify_tracks'
      id column: 'id'
      trackId column: 'track_id', unique: true
      trackName column: 'track_name'
      imageUrl column: 'image_url', type: 'text'
      spotifyUrl column: 'spotify_url'
      popularity column: 'popularity'
      fetchedAt column: 'fetched_at'
      spotifyArtist column: 'spotify_artist_id'
   }



   static constraints = {
      trackId nullable: false, blank: false
      trackName nullable: false, blank: false
      spotifyArtist nullable: false
   }
}
