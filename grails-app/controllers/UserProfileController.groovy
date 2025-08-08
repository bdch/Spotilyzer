import grails.converters.JSON
import grails.web.Controller
import org.bdch.User
import org.bdch.services.UserService
import util.AbstractController

@Controller
class UserProfileController extends AbstractController {

    UserService userService

    def deleteUserProfile() {
        logger.info("Attempting to delete user profile")
       User currentUser = getCurrentUser()
       if (!currentUser) {
          render(status: 401, text: "NO USER IN CONTEXT")
          return
       }
       def result = userService.deleteUser(currentUser.username)
       render(status: result, text: "User profile deleted successfully"  as JSON)
       redirect("/") // Redirect to home page after deletion
    }


    def renderUserProfilePage() {
        renderViewFromStatic("userProfilePage")
    }
}
