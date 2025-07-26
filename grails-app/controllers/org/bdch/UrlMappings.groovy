package org.bdch

class UrlMappings {
    static mappings = {
        "/"(controller: "login", action: "loginPage")

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
