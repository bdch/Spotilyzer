import grails.converters.JSON
import grails.web.Controller
import org.bdch.services.SpotifyService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller
class DashboardController {

   Logger logger = LoggerFactory.getLogger(DashboardController.class)


   SpotifyService spotifyService

   def getDashboardData() {
      try {
         def profile = spotifyService.getUserProfile(request)

         render([
            profile: profile
         ] as JSON)
         logger.info("Collecting dashboard data for current user")
      } catch (Exception e) {
         logger.error("Error collecting dashboard data: ${e.message}", e)
         render(status: 500, text: "Error collecting dashboard data: ${e.message}")
      }
   }
}
