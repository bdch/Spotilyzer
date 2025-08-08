class UrlMappings {
   static mappings = {

      // Login Page
      "/"(controller: "login", action: "renderLoginPage")
      // TODO This kinda sucks since we set the default to '/' instead of a specific controller/action
      "/registerPage"(controller: "auth", action: "renderRegisterPage")
      "/auth/register"(controller: "auth", action: "register")
      "/loginPage"(controller: "login", action: "login", method: "POST")

      // Home Page
      "/home"(controller: "homeDelegate", action: "renderHomePage")

      // User Profile
      "/userProfilePage"(controller: "userProfile", action: "renderUserProfilePage")
      "/userProfilePage/delete"(controller: "userProfile", action: "deleteUserProfile", method: "POST")

      // Auth for Spotify
      "/auth/callback"(controller: "auth", action: "callbackHandling")
      "/auth/spotify"(controller: "auth", action: "spotifyLogin")


      "/$controller/$action?/$id?(.$format)?" {
         constraints {
            // apply constraints here
         }
      }

      // Error Pages
      "500"(view: '/login') // At error, redirect to login page
      "404"(view: '/notFound')
   }
}
