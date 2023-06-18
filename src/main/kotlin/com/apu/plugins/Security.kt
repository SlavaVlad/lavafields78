package com.apu.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

data class UserSession(val name: String, val count: Int) : Principal

fun Application.configureSecurity() {
//    install(Sessions) {
//        cookie<UserSession>("user_session") {
//            cookie.path = "/"
//            cookie.maxAgeInSeconds = 60
//        }
//    }
//    install(Authentication) {
//        form("auth-form") {
//            userParamName = "username"
//            passwordParamName = "password"
//            validate { credentials ->
//                if (credentials.name == "test" && credentials.password == "test") {
//                    UserIdPrincipal(credentials.name)
//                } else {
//                    null
//                }
//            }
//            challenge {
//                call.respond(HttpStatusCode.Unauthorized)
//            }
//        }
//        session<UserSession>("auth-session") {
//            validate { session ->
//                if (session.name.isNotEmpty()) {
//                    session
//                } else {
//                    null
//                }
//            }
//            challenge {
//                if (it == null) {
//                    call.respondRedirect("/login")
//                }
//            }
//        }
//    }
}
