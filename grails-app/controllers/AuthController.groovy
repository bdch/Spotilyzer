import grails.converters.JSON
import org.bdch.services.AuthService
import util.AbstractController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

class AuthController extends AbstractController {

   Logger logger = LoggerFactory.getLogger(AuthController.class)


   AuthService authService

   @Transactional
   def register() {
      logger.info("Trying to register a new user")
      def jsonText = request.reader.text
      def json = JSON.parse(jsonText)

      def username = json?.username?.toString() // THis fucking shit is not even Typesafe HOLY SMH
      def password = json?.password?.toString()

      def result = authService.registerUser(username, password)

      render result as JSON
   }


   def spotifyLogin() {
      try {
         def clientId = '788cbe36aea2447da6c6ad64c7208ab2' // Holy shit, lets hardcode this
         def redirectUri = URLEncoder.encode('http://localhost:8080/auth/callback', 'UTF-8')
         def scope = URLEncoder.encode('user-read-private user-read-email playlist-read-private', 'UTF-8')

         def authUrl = "https://accounts.spotify.com/authorize" +
            "?response_type=code" +
            "&client_id=${clientId}" +
            "&scope=${scope}" +
            "&redirect_uri=${redirectUri}"

         redirect(url: authUrl)
      } catch (Exception e) {
         logger.error("Error at sending OAuth: $e")
      }
   }

   def callbackHandling() {
      def code = params.code
      if (!code) {
         logger.info("Authorization failed")
         render "Authorization failed"
      }

      def clientId = '788cbe36aea2447da6c6ad64c7208ab2'
      def clientSecret = '13d06a46a7b94e8cb549d2f58d2e5ef1'
      def redirectUri = 'http://localhost:8080/auth/callback'

      def response = new URL('https://accounts.spotify.com/api/token').openConnection()
      response.setRequestMethod('POST')
      response.doOutput = true

      response.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
      response.outputStream.withWriter("UTF-8") { writer ->
         writer.write("grant_type=authorization_code" +
            "&code=${code}" +
            "&redirect_uri=${URLEncoder.encode(redirectUri, 'UTF-8')}" +
            "&client_id=${clientId}" +
            "&client_secret=${clientSecret}")
      }

      def result = response.inputStream.text
      def tokenData = JSON.parse(result)

      // Save the token
      session.spotifyAccessToken = tokenData.access_token
      session.spotifyRefreshToken = tokenData.refresh_token

      logger.info("Spotify access token: ${session.spotifyAccessToken}")
      render "Spotify account linked successfully!"
   }

   def renderRegisterPage() {
      renderViewFromStatic("registerPage")
   }
}
