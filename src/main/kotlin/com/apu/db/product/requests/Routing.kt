package com.apu.db.product.requests

import app.common.server.Command
import app.common.server.CommandServer
import com.apu.db.product.ProductDao
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import java.util.*

fun Application.routingForProducts(dao: ProductDao) {
    val commandServer = CommandServer(dao)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/login") {
            data class Auth(
                val name: String,
                val pass: String
            )
            val user = call.receive<Auth>()
            if (user.pass == "1234") {
                val jwtSecret = "secretforme"
                val token = JWT.create()
                    .withExpiresAt(Date(System.currentTimeMillis() + 600000))
                    .sign(Algorithm.HMAC256(jwtSecret))
                call.respond(hashMapOf("token" to token))
            }
        }

        authenticate("auth-jwt") {
            get("/hello") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
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
    }
}
