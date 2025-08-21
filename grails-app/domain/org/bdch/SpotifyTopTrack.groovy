package org.bdch

import java.sql.Timestamp

class SpotifyTopTrack {

   Long id
   Long userSpotifyId
   String trackId
   String trackName
   Integer popularity
   Timestamp fetchedAt

   SpotifyTrack spotifyTrack

   String timeRange // e.g. "short_term", "medium_term", "long_term"
   // NOTE:  "medium_term" is default according to Spotify API documentation:
   // https://developer.spotify.com/documentation/web-api/reference/get-users-top-artists-and-tracks

   static mapping = {
      table 'spotify_top_tracks'
      id column: 'id'
      userSpotifyId column: 'user_spotify_id'
      trackId column: 'track_id'
      trackName column: 'track_name'
      popularity column: 'popularity'
      fetchedAt column: 'fetched_at'
      timeRange column: 'time_range'
      spotifyTrack column: 'spotify_track_id'
   }

   static constraints = {
   }
}
