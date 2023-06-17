package com.apu.db.utils

import com.apu.db.product.ProductDao
import com.apu.db.product.averageBy
import persistence.database.product.Person
import persistence.database.product.Product

class ProductCollectionInfo(collection: ProductDao) {
    val products = collection.getProducts()

    private fun getMostExpensiveProduct(): Product? {
        return products.maxByOrNull { it.price?: -1 }
    }

    fun getAveragePrice(): Double {
        return products.averageBy { it.price?.toDouble() ?: 0.0 }
    }

    fun getOwners(): Set<Person?> {
        return products.map { it.owner }.toSet()
    }

    override fun toString(): String {
        return "Collection size = ${products.size}" +
                "\nMost expensive product is ${getMostExpensiveProduct()?.name0} id=${getMostExpensiveProduct()?.id}" +
                "\nAverage products price = ${getAveragePrice()}" +
                "\nowners list: ${getOwners()}"
    }
}