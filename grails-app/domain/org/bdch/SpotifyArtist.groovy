package org.bdch

import com.vladmihalcea.hibernate.type.array.StringArrayType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef

import java.sql.Timestamp

@TypeDef(name = "string-array", typeClass = StringArrayType.class)
class SpotifyArtist {

   Long id
   Long userSpotifyId
   String artistId
   String artistName

   @Type(type = "string-array")
   String[] genres // this is custom type def, for reference see:
   // https://www.javadoc.io/doc/com.vladmihalcea/hibernate-types-52/1.1.2/com/vladmihalcea/hibernate/type/array/StringArrayType.html

   Integer position
   Timestamp fetchedAt

   static hasMany = [spotifyTracks: SpotifyTrack]

   static mapping = {
      table 'spotify_artists'
      id column: 'id'
      userSpotifyId column: 'user_spotify_id'
      artistId column: 'artist_id'
      artistName column: 'artist_name'
      genres column: 'genres'
      position column: 'position'
      fetchedAt column: 'fetched_at'
   }


   static constraints = {
      artistId nullable: false, blank: false
      artistName nullable: false, blank: false
      userSpotifyId nullable: false
      fetchedAt nullable: true
      position nullable: true
      genres nullable: true
   }
}
