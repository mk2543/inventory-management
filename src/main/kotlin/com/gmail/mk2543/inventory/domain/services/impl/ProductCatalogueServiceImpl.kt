package com.gmail.mk2543.inventory.domain.services.impl

import com.gmail.mk2543.inventory.domain.models.*
import com.gmail.mk2543.inventory.domain.services.ArticleInventoryService
import com.gmail.mk2543.inventory.domain.services.ProductCatalogueService
import com.gmail.mk2543.inventory.repositories.ProductsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductCatalogueServiceImpl(
    private val productsRepository: ProductsRepository,
    private val articleInventoryService: ArticleInventoryService,
) : ProductCatalogueService {

    @Transactional(readOnly = true)
    override fun fetchProductsBy(warehouseId: WarehouseId, productIds: List<ProductId>): List<CatalogueProduct> {
        val products = productsRepository.findProductsByIds(productIds)
        if (products.size != productIds.size) {
            val notFoundIds = productIds.filter { !products.containsKey(it) }
            throw ProductsNotFound(notFoundIds)
        }

        return convertToCatalogueProducts(products.values, warehouseId)
    }

    @Transactional(readOnly = true)
    override fun fetchAllProductsBy(warehouseId: WarehouseId): List<CatalogueProduct> {
        val products = productsRepository.getAll()
        return convertToCatalogueProducts(products, warehouseId)
    }

    private fun convertToCatalogueProducts(
        products: Collection<Product>,
        warehouseId: WarehouseId
    ): List<CatalogueProduct> {
        val requiredArticles = products.flatMap { it.articles.keys }.toSet()
        val currentInventory =
            articleInventoryService.fetchCurrentInventory(warehouseId, requiredArticles)

        return products.map {
            fetchCatalogueProduct(it, currentInventory)
        }
    }

    private fun fetchCatalogueProduct(product: Product, currentInventory: Map<ArticleId, Int>): CatalogueProduct {
        val availableProductQuantity = product.articles.map { (articleId, requiredQuantity) ->
            val quantityForArticle = currentInventory[articleId] ?: 0
            quantityForArticle / requiredQuantity
        }.min()
        return CatalogueProduct(product.id!!, product.name, availableProductQuantity)
    }
}