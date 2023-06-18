package com.apu.db.product//package app.database

import com.apu.data.ExecutionResult
import com.apu.plugins.DatabaseFactory.dbQuery
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import persistence.database.product.Product
import java.sql.Timestamp
import java.time.Instant
import java.util.*

class ProductRepository : ProductDao {

    private val products = IMC()

    override fun getProducts(): Set<Product> {
        return runBlocking {
            products.mutex.withLock {
                products.products
            }
        }
    }

    override suspend fun addProduct(product: Product, onResult: (ExecutionResult) -> Unit) {
        try {
                dbQuery {
                    val owner = ProductSchema.User.select {
                        ProductSchema.User.name eq (product.owner?.name?: "")
                    }.firstOrNull()?: throw MissingFieldException("owner not exists")
                    val ownerId = owner[ProductSchema.User.id]
                    val metadata = ProductSchema.Metadata.insert {
                        it[createdBy] = ownerId
                        it[createdAt] = Instant.now()
                    }
                    ProductSchema.Products.insert {
                        it[name] = product.name0.toString()
                        it[coordinate_x] =
                            product.coordinates?.x?.toFloat() ?: throw MissingFieldException("coordinate_x")
                        it[coordinate_y] =
                            product.coordinates.y?.toFloat() ?: throw MissingFieldException("coordinate_y")
                        it[price] = product.price?.toInt() ?: throw MissingFieldException("price")
                        it[this.owner] = ownerId
                        it[this.metadata] = metadata[ProductSchema.Metadata.id]
                        it[partNumber] = product.partNumber.toString()
                        it[unitOfMeasure] = product.unitOfMeasure ?: throw MissingFieldException("unitOfMeasure")
                    }
                }
            products.addProduct(product)
            onResult(ExecutionResult.error("Product added"))
        } catch (e: Exception) {
            onResult(ExecutionResult.error("Product not added cause: ${e.message}"))
        }
    }

    override suspend fun removeProductById(id: Long, onResult: (ExecutionResult) -> Unit) = run {
        try {
            dbQuery {
                with(ProductSchema.Products) {
                    deleteWhere { ProductSchema.Products.id eq id }
                }
                val product = products.products.firstOrNull { it.id == id }
                if (product != null) {
                    products.remove(product)
                    onResult(ExecutionResult.success("Product removed"))
                } else {
                    onResult(ExecutionResult.error("Product not found"))
                }
            }
        } catch (e: Exception) {
            onResult(ExecutionResult.error("Product not removed cause: ${e.message}"))
        }
    }

    override fun getSize(): Int {
        return products.products.size
    }

    override fun compareMax(product: Product): Boolean {
        return products.products.comparator().compare(product, products.products.max()) > 0
    }

    override fun compareMin(product: Product): Boolean {
        return products.products.comparator().compare(product, products.products.max()) < 0
    }

    override suspend fun removeAllGreaterThan(product: Product, onResult: (ExecutionResult) -> Unit): Int {
        try {
            dbQuery {
                with(ProductSchema.Products) {
                    deleteWhere { price eq (product.price?.toInt() ?: throw MissingFieldException("price")) }
                    var count = 0
                    while (products.products.comparator().compare(product, products.products.max()) > 0) {
                        products.remove(products.products.max())
                        count++
                    }
                    return@dbQuery count
                }
            }
        } catch (e: Exception) {
            onResult(ExecutionResult.error("Products not removed cause: ${e.message}"))
        }
        return 0
    }

    override fun filter(predicate: (Product) -> Boolean) = products.products.filter { predicate(it) }

    override suspend fun clear(onResult: (ExecutionResult) -> Unit) = dbQuery {
        try {
            with(ProductSchema.Products) {
                deleteAll()
            }
            products.clear()
        } catch (e: Exception) {
            onResult(ExecutionResult.error("Products not cleared cause: ${e.message}"))
        }
        onResult(ExecutionResult.success("Products cleared"))
    }

    class MissingFieldException(fieldName: String) : Exception("Field for property '$fieldName' is missing")

}

fun <T> Iterable<T>.averageBy(selector: (T) -> Number): Double {
    var sum = 0.0
    var count = 0
    for (element in this) {
        sum += selector(element).toDouble()
        count++
    }
    return if (count > 0) sum / count else 0.0
}

class IMC {
    val products: TreeSet<Product> = TreeSet<Product>()
    val mutex = Mutex()

    suspend fun addProduct(p: Product) {
        mutex.withLock {
            products.add(p)
        }
    }
    suspend fun remove(p: Product) {
        mutex.withLock {
            products.remove(p)
        }
    }
    suspend fun clear() {
        mutex.withLock {
            products.clear()
        }
    }
}