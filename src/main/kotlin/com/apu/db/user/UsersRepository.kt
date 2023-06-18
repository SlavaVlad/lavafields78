package com.apu.db.user

import com.apu.data.ExecutionResult
import com.apu.db.product.ProductSchema
import com.apu.plugins.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UsersRepository : UsersDao {
    override suspend fun addUser(user: User, onResult: (ExecutionResult) -> Unit): Boolean = dbQuery {
        with(ProductSchema.User) {
            insert {
                it[name] = user.name
                it[passwordHashed] = user.password
            }
        }
        return@dbQuery false
    }

    override suspend fun getUserByName(name: String): User? = dbQuery {
        with(ProductSchema.User) {
            select { ProductSchema.User.name eq name }.firstOrNull()
        }?.let {
            // result row to User object
            User(it)
        } ?: return@dbQuery null
    }

    override suspend fun getUserById(id: Long): User? = dbQuery {
        with(ProductSchema.User) {
            select { ProductSchema.User.id eq id }.firstOrNull()
        }?.let {
            User(it)
        } ?: return@dbQuery null
    }

    override suspend fun removeUserById(id: Long, onResult: (ExecutionResult) -> Unit): Boolean = dbQuery {
        try {
            with(ProductSchema.User) {
                deleteWhere { ProductSchema.User.id eq id}
            }
        } catch (e: Exception) {
            onResult(ExecutionResult.error("User not removed cause: ${e.message}"))
            return@dbQuery false
        }
        return@dbQuery true
    }

    override suspend fun getSize(): Int = dbQuery {
        with(ProductSchema.User) {
            selectAll().count()
        }.toInt()
    }

    override suspend fun clear() {
        dbQuery {
            with(ProductSchema.User) {
                deleteAll()
            }
        }
    }

}