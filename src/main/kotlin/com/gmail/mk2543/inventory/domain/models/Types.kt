package com.gmail.mk2543.inventory.domain.models

data class ProductId(val value: Long)
data class Product(val id: ProductId? = null, val name: String, val articles: Map<ArticleId, Int>)
data class CatalogueProduct(val id: ProductId, val name: String, val availableQuantity: Int)
data class OrderLine(val productId: ProductId, val quantity: Int)
data class ArticleId(val value: Long)
data class Article(val id: ArticleId, val name: String, val version: Long = 0)
data class WarehouseId(val value: Long)
data class ArticleInventory(val articleId: ArticleId, val warehouseId: WarehouseId, val quantity: Int, val version: Long)

data class SaveArticleDefinitionRequest(val articleId: ArticleId, val articleName: String, val inventoryOverrides: Map<WarehouseId, Int>) {
    init {
        require(articleName.isNotBlank()) { "Article name cannot be blank" }
        require(inventoryOverrides.values.all { it >= 0 }) { "Quantity cannot be negative" }
    }
}
