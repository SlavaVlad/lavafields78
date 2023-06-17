package com.apu.db.product

import persistence.database.product.Person
import persistence.database.product.Product

suspend fun ProductDao.info(): String {
    val products = this.getProducts()

    fun getMostExpensiveProduct(): Product? {
        return products.maxByOrNull { it.price?: -1 }
    }

    fun getAveragePrice(): Double {
        return products.averageBy { it.price?.toDouble() ?: 0.0 }
    }

    fun getOwners(): Set<Person?> {
        return products.map { it.owner }.toSet()
    }

    fun textInfo(): String {
        return "Collection size = ${products.size}" +
                "\nMost expensive product is ${getMostExpensiveProduct()?.name0} id=${getMostExpensiveProduct()?.id}" +
                "\nAverage products price = ${getAveragePrice()}" +
                "\n owners list: ${getOwners()}"
    }

    return textInfo()
}