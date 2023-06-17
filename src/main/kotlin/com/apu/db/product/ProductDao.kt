package com.apu.db.product

import com.apu.callback.ExecutionResult
import persistence.database.product.Product

interface ProductDao {
    fun getProducts(): Set<Product>
    suspend fun addProduct(product: Product, onResult : (ExecutionResult) -> Unit = {})
    suspend fun removeProductById(id: Long, onResult : (ExecutionResult) -> Unit = {})
    fun getSize(): Int
    fun compareMax(product: Product): Boolean
    fun compareMin(product: Product): Boolean
    suspend fun removeAllGreaterThan(product: Product, onResult : (ExecutionResult) -> Unit = {}): Int
    fun filter(predicate: (Product) -> Boolean) : List<Product>
    suspend fun clear(onResult : (ExecutionResult) -> Unit = {})
}
