

import util.AbstractController

class HomeDelegateController extends AbstractController {


   def renderHomePage() {
      renderViewFromStatic("mainPage")
   }
}
