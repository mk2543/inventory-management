package com.gmail.mk2543.inventory.controllers


import com.gmail.mk2543.inventory.domain.models.*

data class OrderLineDto(val productId: String, val quantity: String) {
    fun toDomain() = OrderLine(ProductId(productId.toLong()), quantity.toInt())
}

data class PurchasedArticlesDto(
    val articles: List<ArticleQuantityDto>
)

data class ProductsDto(
    val products: List<ProductDto>
)

data class ProductDto(
    val id: String? = null,
    val name: String,
    val containArticles: List<ArticleQuantityDto>
) {
    fun toDomain() = Product(
        name = name,
        articles = containArticles.associate { ArticleId(it.artId.toLong()) to it.amountOf.toInt() }
    )
}

fun Product.toDto() = ProductDto(
    id = id?.value.toString(),
    name = name,
    containArticles = articles.map { (articleId, amount) ->
        ArticleQuantityDto(
            articleId.value.toString(),
            amount.toString()
        )
    }
)

data class ArticleQuantityDto(
    val artId: String,
    val amountOf: String
)

fun Map.Entry<ArticleId, Int>.toDto() = ArticleQuantityDto(
    key.value.toString(),
    value.toString()
)

data class InventoryDto(
    val inventory: List<ArticleDto>
)

data class ArticleDto(
    val artId: String,
    val name: String,
    val stock: Int
) {
    fun toDomain(warehouseId: WarehouseId) = SaveArticleDefinitionRequest(
        ArticleId(artId.toLong()),
        name,
        mapOf(warehouseId to stock),
    )
}

data class CatalogueDto(
    val products: List<CatalogueProductDto>
)

data class CatalogueProductDto(
    val id: String,
    val name: String,
    val availableQuantity: String
)

data class MissingArticlesDto(
    val missingArticles: List<ArticleQuantityDto>
)

data class UnknownArticlesDto(
    val unknownArticles: List<String>
)

data class UnknownProductsDto(
    val unknownProducts: List<String>
)

fun CatalogueProduct.toDto() = CatalogueProductDto(
    id = id.value.toString(),
    name = name,
    availableQuantity = availableQuantity.toString()
)