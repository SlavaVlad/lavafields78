package com.apu.db.product

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import persistence.database.product.UnitOfMeasure

class ProductSchema(val database: Database) {
    object Products : Table() { // Сам продукт
        val id = long("id").autoIncrement()
        val name = varchar("name", 128)
        val coordinate_x = float("coordinate_x")
        val coordinate_y = float("coordinate_y")
        val price = integer("price")
        val partNumber = varchar("part_number", 32)
        val unitOfMeasure = enumeration("unit_of_measure", UnitOfMeasure::class)
        val owner = long("owner").references(User.id)
        val metadata = long("metadata").references(Metadata.id)

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    object Metadata : Table() { // Дополнительная информация о продукте
        val id = long("id").autoIncrement()
        val createdBy = long("created_by").references(User.id)
        val createdAt = timestamp("created_at")

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    object User : Table() { // Кто создал продукт
        val id = long("id").autoIncrement()
        val name = varchar("name", 128)
        val passwordHashed = varchar("password", 128)
        val height = integer("height")
        val weight = integer("weight")

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Products, Metadata, User)
        }
    }
}
