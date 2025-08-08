import grails.converters.JSON
import org.bdch.User
import org.bdch.services.UserService
import util.AbstractController

class UserProfileController extends AbstractController {

    UserService userService

    def deleteUserProfile() {
        logger.info("Attempting to delete user profile")
       User currentUser = getCurrentUser()
       def result = userService.deleteUser(currentUser.username)
       render(status: result, text: "User profile deleted successfully"  as JSON)
    }


    def renderUserProfilePage() {
        renderViewFromStatic("userProfilePage")
    }
}
