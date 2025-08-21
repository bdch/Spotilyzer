import grails.converters.JSON
import org.bdch.SpotifyUser
import org.bdch.User
import org.bdch.services.AuthService
import org.bdch.services.SpotifyService
import util.AbstractController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

@Transactional
class AuthController extends AbstractController {

   Logger logger = LoggerFactory.getLogger(AuthController.class)


   AuthService authService
   SpotifyService spotifyService

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
         // Get current user's session key
         // I think during the handshake, spotify redirects from `localhost:8080` to `127.0.0.1:8080`.
         // There could be a difference in domain cookies, making the session-cookie we set, lost during
         // OAuth flow.
         String sessionKey = getCookieValue(request, 'sessionKey')
         if (!sessionKey) {
            logger.error("No session key found when initiating Spotify login")
            render "You must be logged in to link your Spotify account"
            return
         }

         logger.info("Found session key for Spotify login: ${sessionKey}")

         def clientId = '788cbe36aea2447da6c6ad64c7208ab2' // Holy shit, lets hardcode this, YES
         // this is such a good idea!
         def redirectUri = URLEncoder.encode('http://127.0.0.1:8080/auth/callback', 'UTF-8')
         def scope = URLEncoder.encode('user-read-private user-read-email playlist-read-private user-top-read', 'UTF-8')
         // Remove curly braces and properly encode the session key

         def state = URLEncoder.encode(sessionKey, 'UTF-8') // Send the session as a state,
         // so it doesn't get lost during OAuth Flow

         def authUrl = "https://accounts.spotify.com/authorize" +
            "?response_type=code" +
            "&client_id=${clientId}" +
            "&scope=${scope}" +
            "&redirect_uri=${redirectUri}" +
            "&state=${state}"

         logger.info("Generated auth URL: ${authUrl}")
         logger.info("Redirecting to Spotify...")
         redirect(url: authUrl)
      } catch (Exception e) {
         logger.error("Error at sending OAuth: $e")
      }
   }

   /**
    * Handles the callback from the Spotify OAuth flow.
    * This method exchanges the authorization code for access and refresh tokens,
    * retrieves the user's Spotify profile data, and links it to the current user.
    * If the user is not authenticated, it returns an error message.
    */
   def callbackHandling() {
      def code = params.code
      def state = params.state

      logger.info("Callback received - code: ${code ? 'present' : 'missing'}, state: ${state}")

      if (!code || !state) {
         logger.info("Authorization failed! Either code or state is missing: ${code}, ${state}")
         render "Authorization failed"
      }

      try {
         // Get current user from request attribute (set by SessionInterceptor)
         User currentUser = request.getAttribute('currentUser') as User

         if (!currentUser) {
            logger.error("No authenticated user found")
            render "You must be logged in to link your Spotify account"
            return
         }
         logger.info("Processing Spotify callback for user: ${currentUser.username}")

         def tokenData = exchangeCodeForTokens(code)

         def spotifyUserData = getSpotifyUserData(tokenData.access_token)

         def spotifyData = [
            spotifyId   : spotifyUserData.id,
            displayName : spotifyUserData.display_name,
            accessToken : tokenData.access_token,
            refreshToken: tokenData.refresh_token,
            expiresIn   : tokenData.expires_in
         ]

         spotifyService.saveSpotifyUser(currentUser, spotifyData)

         logger.info("Successfully linked Spotify account '${spotifyUserData.display_name}' (${spotifyUserData.id}) to user: ${currentUser.username}")
         redirect(url: "/?spotify=linked")
      } catch (Exception e) {
         logger.error("Error linking Spotify account: ${e.message}", e)
         redirect(url: "/?error=spotify_link_failed")
      }
   }

   private def exchangeCodeForTokens(String code) {
      def clientId = '788cbe36aea2447da6c6ad64c7208ab2'
      def clientSecret = '13d06a46a7b94e8cb549d2f58d2e5ef1'
      def redirectUri = 'http://127.0.0.1:8080/auth/callback'

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
      return JSON.parse(result)
   }

   private def getSpotifyUserData(String accessToken) {
      def response = new URL('https://api.spotify.com/v1/me').openConnection()
      response.setRequestMethod('GET')
      response.setRequestProperty("Authorization", "Bearer ${accessToken}")

      if (response.responseCode != 200) {
         throw new RuntimeException("Failed to get Spotify user info: ${response.responseCode}")
      }

      def result = response.inputStream.text
      return JSON.parse(result)
   }

   User getCurrentAuthenticatedUser(request) {
      User currentUser = request.getAttribute('currentUser') as User

      if (!currentUser) {
         // Fallback: manually validate session if attribute not set
         String sessionKey = getCookieValue(request, 'sessionKey')
         if (sessionKey) {
            def validation = authService.validateSession(sessionKey)
            if (validation.valid) {
               currentUser = validation.user
            }
         }
      }
      return currentUser
   }

   /**
    * New endpoint to check if user has Spotify linked
    */
   def spotifyStatus() {
      User currentUser = getCurrentAuthenticatedUser(request)

      if (!currentUser) {
         response.status = 401
         render([error: "Not authenticated"] as JSON)
         return
      }

      SpotifyUser spotifyUser = SpotifyUser.findByUser(currentUser)

      def result = [
         hasSpotify        : spotifyUser != null,
         spotifyDisplayName: spotifyUser?.displayName,
         spotifyId         : spotifyUser?.spotifyId
      ]

      render result as JSON
   }

   /**
    * Endpoint to unlink Spotify account
    */
   def unlinkSpotify() {
      User currentUser = getCurrentAuthenticatedUser(request)

      if (!currentUser) {
         response.status = 401
         render([error: "Not authenticated"] as JSON)
         return
      }

      SpotifyUser spotifyUser = SpotifyUser.findByUser(currentUser)

      if (!spotifyUser) {
         spotifyUser.delete(flush: true)
         logger.info("Unlinked Spotify account for user: ${currentUser.username}")
         render([success: "Spotify account unlinked"] as JSON)
      } else {
         render([error: "No Spotify account found"] as JSON)
      }
   }

   def getUserProfileFromWebApi() {

   }

   def renderRegisterPage() {
      renderViewFromStatic("registerPage")
   }
}
