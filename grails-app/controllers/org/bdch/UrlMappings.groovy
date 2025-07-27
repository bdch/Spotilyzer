package org.bdch

class UrlMappings {
    static mappings = {

        // Login Page
        "/"(controller: "login", action: "renderLoginPage")
        "/register"(controller: "auth", action: "register")

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        // Error Pages
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
