package org.bdch

class UrlMappings {
   static mappings = {

      // Login Page
      "/"(controller: "login", action: "renderLoginPage") // TODO This kinda sucks since we set the default to '/' instead of a specific controller/action
      "/registerPage"(controller: "auth", action: "renderRegisterPage")
      "/auth/register"(controller: "auth", action: "register")
      "/loginPage"(controller: "login", action: "login", method: "POST")

      // Home Page
      "/home"(controller: "homeDelegate", action: "renderHomePage")

      "/$controller/$action?/$id?(.$format)?" {
         constraints {
            // apply constraints here
         }
      }

      // Error Pages
      "500"(view: '/error')
      "404"(view: '/notFound')
   }
}
