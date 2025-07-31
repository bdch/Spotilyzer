package org.bdch

import org.bdch.util.AbstractController

class HomeDelegateController extends AbstractController {


   def renderHomePage() {
      renderViewFromStatic("mainPage")
   }
}
