package com.apu.db.user

import com.apu.data.ExecutionResult
import com.apu.db.ProductSchema
import org.jetbrains.exposed.sql.ResultRow

data class User(
    val id: Long,
    val name: String,
    val password: String
) {
    constructor(row: ResultRow) : this(
        id = row[ProductSchema.User.id],
        name = row[ProductSchema.User.name],
        password = row[ProductSchema.User.passwordHashed]
    )
}

interface UsersDao {
    suspend fun addUser(user: User, onResult: (ExecutionResult) -> Unit): Boolean
    suspend fun getUserByName(name: String): User?
    suspend fun getUserById(id: Long): User?
    suspend fun removeUserById(id: Long, onResult: (ExecutionResult) -> Unit): Boolean
    suspend fun getSize(): Int
    suspend fun clear()
}