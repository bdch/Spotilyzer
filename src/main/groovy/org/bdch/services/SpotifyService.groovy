package org.bdch.services

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.bdch.SpotifyArtist
import org.bdch.SpotifyTopTrack
import org.bdch.SpotifyTrack
import org.bdch.SpotifyUser
import org.bdch.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Timestamp

@Transactional
class SpotifyService {

   Logger logger = LoggerFactory.getLogger(SpotifyService.class)

   private static final long CACHE_TTL_MS = 10 * 60 * 1000 // 10 minutes cache TTL

   def saveSpotifyUser(User user, Map spotifyData) {

      SpotifyUser existingSpotifyUser = SpotifyUser.findByUser(user)

      if (existingSpotifyUser) {
         // We update the existing SpotifyUser
         existingSpotifyUser.accessToken = spotifyData.accessToken
         existingSpotifyUser.refreshToken = spotifyData.refreshToken
         existingSpotifyUser.tokenExpiration = new Timestamp(
            System.currentTimeMillis() + (spotifyData.expiresIn * 1000)
         )
         existingSpotifyUser.displayName = spotifyData.displayName ?: null
         existingSpotifyUser.save(flush: true, failOnError: true)
         return existingSpotifyUser
      } else {
         // Or we create a new SpotifyUser
         SpotifyUser newSpotifyUser = new SpotifyUser(
            user: user,
            spotifyId: spotifyData.spotifyId,
            displayName: spotifyData.displayName,
            accessToken: spotifyData.accessToken,
            refreshToken: spotifyData.refreshToken,
            tokenExpiration: new Timestamp(
               System.currentTimeMillis() + (spotifyData.expiresIn * 1000)
            )
         )
         if (!newSpotifyUser.validate()) {
            throw new IllegalArgumentException("Invalid Spotify user data: ${newSpotifyUser.errors}")
         }
         newSpotifyUser.save(flush: true, failOnError: true)
      }
   }

   // Remove or refactor since we have a new method for this
   @Deprecated(forRemoval = true, since = "1.0.0")
   def getValidAccessToken(User user) {
      SpotifyUser spotifyUser = SpotifyUser.findByUser(user)

      if (!spotifyUser) {
         logger.info("No Spotify user found for: ${user.username}")
         return null
      }
      // Check if token is expired (with 5 minute buffer)
      def now = System.currentTimeMillis()
      def tokenExpiry = spotifyUser.tokenExpiration.time

      if (now >= (tokenExpiry - 5 * 60 * 1000)) { // 5 minutes before expiration
         // Token is expired, we should refresh it
         return refreshAccessToken(spotifyUser)
      }
      return spotifyUser.accessToken
   }

   static boolean isTokenExpired(SpotifyUser spotifyUser) {
      return System.currentTimeMillis() >= spotifyUser.tokenExpiration.time
   }

   def refreshAccessToken(SpotifyUser spotifyUser) {
      try {
         def clientId = '788cbe36aea2447da6c6ad64c7208ab2'
         def clientSecret = '13d06a46a7b94e8cb549d2f58d2e5ef1'

         def response = new URL('https://accounts.spotify.com/api/token').openConnection()
         response.setRequestMethod('POST')
         response.doOutput = true

         def auth = "${clientId}:${clientSecret}".bytes.encodeBase64().toString()
         response.setRequestProperty("Authorization", "Basic ${auth}")
         response.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

         response.outputStream.withWriter("UTF-8") { writer ->
            writer.write("grant_type=refresh_token&refresh_token=${spotifyUser.refreshToken}")
         }

         def result = response.inputStream.text
         def tokenData = JSON.parse(result)

         spotifyUser.accessToken = tokenData.access_token
         if (tokenData.refresh_token) {
            spotifyUser.refreshToken = tokenData.refresh_token
         }
         spotifyUser.tokenExpiration = new Timestamp(System.currentTimeMillis() + (tokenData.expires_in * 1000))

         spotifyUser.save(flush: true, failOnError: true)

         return spotifyUser.accessToken
      } catch (Exception e) {
         logger.error("Failed to refresh Spotify access token", e)
         throw new RuntimeException("Failed to refresh Spotify access token")
      }
   }

   // -----------------------------
   // PROFILE
   // -----------------------------
   def getUserProfile(User currentUser) {
      SpotifyUser currentSpotifyUser = SpotifyUser.findByUser(currentUser)

      if (!currentSpotifyUser) {
         logger.error("No authenticated Spotify User found")
      }

      if (isTokenExpired(currentSpotifyUser)) {
         logger.info("Spotify access token is not valid for user: ${currentUser.username}")
         logger.info("Attempting to refresh access token...")
         refreshAccessToken(currentSpotifyUser)
         logger.info("Refresh access token success!")
         return fetchSpotifyProfile(currentSpotifyUser)
         // and send
      } else {
         logger.info("Spotify access token is still valid for user: ${currentUser.username}")
         return fetchSpotifyProfile(currentSpotifyUser)
      }
   }

   private def fetchSpotifyProfile(SpotifyUser currentSpotifyUser) {
      def accessToken = currentSpotifyUser.accessToken
      def conn = new URL("https://api.spotify.com/v1/me").openConnection()
      conn.setRequestProperty("Authorization", "Bearer ${accessToken}")

      def response = conn.inputStream.text
      def responseJson = JSON.parse(response)

      currentSpotifyUser.displayName = responseJson.display_name

      if (responseJson.images && responseJson.images.size() > 0) {
         currentSpotifyUser.profileImageUrl = responseJson.images[0].url ?: currentSpotifyUser.profileImageUrl
         if (responseJson.images.size() > 1) {
            currentSpotifyUser.profileImageUrlSmall = responseJson.images[1].url ?: currentSpotifyUser.profileImageUrlSmall
         }
      }

      if (!currentSpotifyUser.save(flush: true)) {
         logger.error("Failed to update SpotifyUser: ${currentSpotifyUser.errors}")
      }

      return [
         id          : responseJson.id,
         displayName : responseJson.display_name,
         images      : responseJson.images ?: [],
         product     : responseJson.product ?: "unknown",
         uri         : responseJson.uri ?: "",
         externalUrls: responseJson.external_urls ?: [:],
      ]
   }

   // -----------------------------
   // TOP TRACKS
   // -----------------------------

   /**
    * Get top tracks - either from cache (database) or fetch fresh from Spotify
    * @param currentUser The current user
    * @param timeRange Time range for top tracks (short_term, medium_term, long_term)
    * @param limit Number of tracks to fetch (max 50)
    * @return List of top tracks
    */
   def getTopTracks(User currentUser, String timeRange = "medium_term", int limit = 20, boolean forceRefresh = false) {
      SpotifyUser spotifyUser = SpotifyUser.findByUser(currentUser)

      if (!spotifyUser) {
         logger.error("No authenticated Spotify User found for top tracks")
         return []
      }

      // 1. Token-Check
      if (isTokenExpired(spotifyUser)) {
         logger.info("Spotify access token expired for user: ${currentUser.username}, refreshing...")
         refreshAccessToken(spotifyUser)
      }

      spotifyUser.refresh()


      if (!spotifyUser.accessToken) {
         logger.error("No access token available for user: ${currentUser.username} even after refresh")
         return []
      }

      // 2. Check cache
      def cachedTracks = getCachedTopTracks(spotifyUser.id, timeRange, limit)
      if (cachedTracks) {
         logger.info("Returning ${cachedTracks.size()} cached top tracks for user: ${currentUser.username}")
         return cachedTracks
      }

      // 3. Fetch from Spotify
      def freshTracks = fetchTopTracksFromSpotify(spotifyUser, timeRange, limit)
      if (freshTracks) {
         cacheTopTracks(spotifyUser.id, timeRange, freshTracks)
      }
      return freshTracks
   }

   private def getCachedTopTracks(Long userSpotifyId, String timeRange, int limit) {
      def cutoff = new Timestamp(System.currentTimeMillis() - CACHE_TTL_MS)

      def cachedTracks = SpotifyTopTrack.findAllByUserSpotifyIdAndTimeRangeAndFetchedAtGreaterThan(
         userSpotifyId,
         timeRange,
         cutoff,
         [max: limit, sort: 'id', order: 'asc']
      )

      if (!cachedTracks) {
         return []
      }

      return cachedTracks.collect { track ->
         [
            id        : track.trackId,
            name      : track.trackName,
            popularity: track.popularity
         ]
      }
   }

   private void cacheTopTracks(Long userSpotifyId, String timeRange, List<Map> tracks) {
      SpotifyTopTrack.executeUpdate(
         "delete from SpotifyTopTrack where userSpotifyId = :uid and timeRange = :range",
         [uid: userSpotifyId, range: timeRange]
      )

      def now = new Timestamp(System.currentTimeMillis())
      tracks.each { track ->
         new SpotifyTopTrack(
            userSpotifyId: userSpotifyId,
            trackId: track.id,
            trackName: track.name,
            popularity: track.popularity,
            timeRange: timeRange,
            fetchedAt: now
         ).save(flush: false, failOnError: true)
      }
      logger.info("Cached ${tracks.size()} top tracks for userSpotifyId=${userSpotifyId}, range=${timeRange}")
   }

   private List<Map> fetchTopTracksFromSpotify(SpotifyUser spotifyUser, String timeRange, int limit) {
      def accessToken = spotifyUser.accessToken
      if (!accessToken) {
         logger.error("No access token in DB for user=${spotifyUser.user.username}")
         return []
      }

      try {
         def url = "https://api.spotify.com/v1/me/top/tracks?time_range=${timeRange}&limit=${limit}"
         def conn = new URL(url).openConnection()
         conn.setRequestProperty("Authorization", "Bearer ${accessToken}")

         def response = conn.inputStream.text
         def responseJson = JSON.parse(response)

         def topTracks = []

         responseJson.items?.each { trackData ->
            try {
               SpotifyTrack track = upsertSpotifyTrack(trackData, spotifyUser)

               SpotifyTopTrack topTrack = new SpotifyTopTrack(
                  userSpotifyId: spotifyUser.id,
                  trackId: track.trackId,
                  trackName: track.trackName,
                  popularity: track.popularity,
                  timeRange: timeRange,
                  fetchedAt: new Timestamp(System.currentTimeMillis())
               ).save(flush: true, failOnError: true)

               topTracks << [
                  id         : track.trackId,
                  name       : track.trackName,
                  popularity : track.popularity,
                  artist     : track.spotifyArtist.artistName,
                  imageUrl   : track.imageUrl,
                  externalUrl: track.spotifyUrl
               ]
            } catch (Exception e) {
               logger.error("Failed to upsert track: ${trackData.id} - ${trackData.name}", e)
            }
         }
         return topTracks
      } catch (Exception e) {
         logger.error("Failed to fetch top tracks from Spotify", e)
         // Skip this track and continue
      }
   }

   private SpotifyTrack upsertSpotifyTrack(Map trackData, SpotifyUser spotifyUser) {
      logger.info("Attempting to upsert Spotify track: ${trackData.id} - ${trackData.name}")

      def artistData = trackData.artists?.getAt(0)
      SpotifyArtist artist = null

      if (artistData) {
         artist = SpotifyArtist.findByArtistId(artistData.id)
         if (!artist) {
            artist = new SpotifyArtist(
               userSpotifyId: spotifyUser.id,
               artistId: artistData.id,
               artistName: artistData.name,
               fetchedAt: new Timestamp(System.currentTimeMillis())
            ).save(flush: true, failOnError: true)
         }
      }

      logger.info("Upserting track for artist: ${artist?.artistName ?: 'Unknown Artist'}")

      SpotifyTrack track = SpotifyTrack.findByTrackId(trackData.id)
      if (!track) {
         track = new SpotifyTrack(
            trackId: trackData.id,
            trackName: trackData.name,
            imageUrl: trackData.album?.images?.getAt(0)?.url,
            spotifyUrl: trackData.external_urls?.spotify ?: "",
            popularity: trackData.popularity ?: 0,
            fetchedAt: new Timestamp(System.currentTimeMillis()),
            spotifyArtist: artist,
            spotifyUser: spotifyUser
         ).save(flush: true, failOnError: true)
      }
      logger.info("Upserted track: ${track.trackName} (ID: ${track.trackId})")
      return track
   }


}

