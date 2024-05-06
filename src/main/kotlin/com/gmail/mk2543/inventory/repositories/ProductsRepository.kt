package com.gmail.mk2543.inventory.repositories

import com.gmail.mk2543.inventory.domain.models.Product
import com.gmail.mk2543.inventory.domain.models.ProductId

interface ProductsRepository {

    fun get(id: ProductId): Product?

    fun getAll(): List<Product>

    fun findProductsByIds(ids: Collection<ProductId>): Map<ProductId, Product>

    fun save(product: Product): Product

    fun saveAll(products: List<Product>): List<Product>

    fun deleteAll()
}