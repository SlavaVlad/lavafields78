package com.apu.plugins

import com.apu.db.product.ProductSchema
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init() {
        val database = Database.connect(
            url = "jdbc:postgresql:///productsmain",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = System.getenv("pgpass") // а вы что думали?)

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
