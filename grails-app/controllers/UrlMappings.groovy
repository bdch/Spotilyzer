class UrlMappings {
   static mappings = {

      // Login Page
      "/"(controller: "login", action: "renderLoginPage")
      // TODO This kinda sucks since we set the default to '/' instead of a specific controller/action
      "/registerPage"(controller: "auth", action: "renderRegisterPage")
      "/auth/register"(controller: "auth", action: "register")
      "/loginPage"(controller: "login", action: "login", method: "POST")

      // Main Dashboard Page: This is only is the rendering
      "/home"(controller: "homeDelegate", action: "renderHomePage")

      // Dashboard Data API as ONE JSON endpoint
      "/api/dashboard/data"(controller: "dashboard", action: "getDashboardData")

      // Spotify Data fetching: Our strategy is to have endpoints for different data fetching
      // This way we can easily extend it in the future and group it into the endpoint above.
      // This way, we are chasing the BFF (Backend for Frontend) pattern.
      "/api/spotify/currentUserProfile"(controller: "spotify", action: "getFreshUserProfile")


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
