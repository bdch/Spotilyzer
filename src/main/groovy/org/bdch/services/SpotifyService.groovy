package org.bdch.services

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.apache.commons.collections.keyvalue.TiedMapEntry
import org.bdch.SpotifyUser
import org.bdch.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest
import java.net.http.HttpRequest
import java.sql.Timestamp

@Transactional
class SpotifyService {

   Logger logger = LoggerFactory.getLogger(SpotifyService.class)

   static def saveSpotifyUser(User user, Map spotifyData) {

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

         // and send
      } else {
         logger.info("Spotify access token is still valid for user: ${currentUser.username}")
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
   }
}
