package com.apu.plugins

import com.apu.db.ProductSchema
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init() {
        val database = Database.connect(
            url = "jdbc:postgresql://localhost/studs",
            driver = "org.postgresql.Driver",
            user = "s367982",
            password = System.getenv("pgpass"),
        )
        ProductSchema(database)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

fun configureDatabases() {
    println("db init started")
    DatabaseFactory.init()
    println("db init finished")
}
