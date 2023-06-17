package com.apu

import com.apu.db.product.ProductRepository
import com.apu.db.product.requests.routingForProducts
import com.apu.plugins.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 80, host = "localhost", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureTemplating()
    configureMonitoring()
    configureSerialization()
    configureAdministration()
    configureSecurity()
    configureDatabases()

    routingForProducts(ProductRepository())
}
