import util.AbstractController

class UserProfileController extends AbstractController {


   def renderUserProfilePage() {
      renderViewFromStatic("userProfilePage")
   }
}
