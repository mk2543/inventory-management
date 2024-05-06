package com.gmail.mk2543.inventory.repositories

import com.gmail.mk2543.inventory.domain.models.Product
import com.gmail.mk2543.inventory.domain.models.ProductId

class InMemoryProductsRepository : ProductsRepository {

    private val productsDb = mutableMapOf<ProductId, Product>()
    private var idGenerator = 0L
    override fun get(id: ProductId): Product? {
        return productsDb[id]
    }

    override fun getAll(): List<Product> {
        return productsDb.values.toList()
    }

    override fun findProductsByIds(ids: Collection<ProductId>): Map<ProductId, Product> {
        return productsDb.filterKeys { ids.contains(it) }
    }

    override fun save(product: Product): Product {
        val productId = product.id ?: ProductId(++idGenerator)
        if (product.id != null) {
            productsDb[productId] = product
        } else {
            productsDb[productId] = product.copy(id = productId)
        }
        return productsDb[productId]!!
    }

    override fun saveAll(products: List<Product>): List<Product> {
        return products.map { save(it) }
    }

    override fun deleteAll() {
        productsDb.clear()
    }
}