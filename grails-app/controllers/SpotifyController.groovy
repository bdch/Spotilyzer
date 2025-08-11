import grails.converters.JSON
import org.bdch.SpotifyUser
import org.bdch.User
import org.bdch.services.SpotifyService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import util.AbstractController


class SpotifyController extends AbstractController {

   Logger logger = LoggerFactory.getLogger(SpotifyController.class)


   SpotifyService spotifyService

   /**
    * Get the current user's Spotify profile.
    * This method retrieves the Spotify profile of the currently logged-in user
    * and refreshes the access token if necessary.
    * It returns the profile data as JSON.
    * */
   def getFreshUserProfile() {
      try {
         User currentUser = request.getAttribute('currentUser') as User
         def data = spotifyService.getUserProfile(currentUser)
         render data as JSON
      }
      catch (Exception e) {
         logger.error("Error getting fresh user profile: ${e.message}", e)
      }
   }

}
