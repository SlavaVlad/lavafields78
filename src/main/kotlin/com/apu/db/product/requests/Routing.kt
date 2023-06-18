package com.apu.db.product.requests

import app.common.server.Command
import app.common.server.CommandServer
import com.apu.db.product.ProductDao
import com.apu.plugins.UserSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun Application.routingForProducts(dao: ProductDao) {
    val commandServer = CommandServer(dao)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

//        authenticate("auth-form") {
//            route("/login") {
//                get {
//                    val userName = call.principal<UserIdPrincipal>()?.name.toString()
//                    call.sessions.set(UserSession(name = userName, count = 1))
//                }
//                post {
//                    val userName = call.principal<UserIdPrincipal>()?.name.toString()
//                    call.sessions.set(UserSession(name = userName, count = 1))
//                }
//            }
//        }

//        authenticate("auth-session") {
            post("/hello") {
                val session = call.sessions.get<UserSession>()
                call.respondText("Hello ${session?.name}! You have visited this page ${session?.count} times.")
            }
            get("/hello") {
                val session = call.sessions.get<UserSession>()
                call.respondText("Hello ${session?.name}! You have visited this page ${session?.count} times.")
            }
            post("/command") {
                try {
                    val command = call.receive<Command>()
                    commandServer.findReferenceOrNull(command.name!!)?.let {
                        it.function(command.args!!.toList()) {
                            runBlocking {
                                call.respond(HttpStatusCode.OK, it)
                            }
                        }
                    } ?: call.respond(HttpStatusCode.BadRequest, "Command not exists ${command.name}")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Cannot deserialize command")
                }
            }
        }
//    }
}
