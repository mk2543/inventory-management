package com.gmail.mk2543.inventory.domain.services.impl

import com.gmail.mk2543.inventory.domain.models.*
import com.gmail.mk2543.inventory.domain.services.ArticleInventoryService
import com.gmail.mk2543.inventory.domain.services.OrderProcessor
import com.gmail.mk2543.inventory.repositories.ProductsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class OrderProcessorImpl(
    private val productsRepository: ProductsRepository,
    private val articleInventoryService: ArticleInventoryService,
) : OrderProcessor {

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    override fun handlePurchase(warehouseId: WarehouseId, orderLines: Set<OrderLine>): Map<ArticleId, Int>{
        val products = fetchRequiredProducts(orderLines)
        val requiredArticlesQuantities = calculateTotalQuantitiesPerOrder(orderLines, products)
        val availableInventory =
            articleInventoryService.fetchCurrentInventory(warehouseId, requiredArticlesQuantities.keys)
        val unavailableArticles = calculateArticleAvailability(requiredArticlesQuantities, availableInventory)

        if (unavailableArticles.isNotEmpty()) {
            throw ArticlesNotAvailable(unavailableArticles)
        }

        applyInventoryChanges(warehouseId, requiredArticlesQuantities)
        return requiredArticlesQuantities
    }

    private fun fetchRequiredProducts(orderLines: Set<OrderLine>): Map<ProductId, Product> {
        val neededProductIds = orderLines.map { it.productId }.toSet()
        val foundProducts = productsRepository.findProductsByIds(neededProductIds)
        if (foundProducts.size != orderLines.size) {
            throw ProductsNotFound(neededProductIds.minus(foundProducts.keys))
        }
        return foundProducts
    }

    private fun calculateTotalQuantitiesPerOrder(
        orderLines: Set<OrderLine>,
        products: Map<ProductId, Product>
    ): Map<ArticleId, Int> {

        return orderLines
            .flatMap { orderLine ->
                val product = products[orderLine.productId]!!
                product.articles.map { articleQuantity ->
                    articleQuantity.key to articleQuantity.value * orderLine.quantity
                }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.sum() }
    }

    private fun calculateArticleAvailability(
        requiredArticleQuantities: Map<ArticleId, Int>,
        availableArticles: Map<ArticleId, Int>
    ): Map<ArticleId, Int> {
        return requiredArticleQuantities.mapNotNull { (articleId, requiredQuantity) ->
            val availableQuantity = availableArticles[articleId] ?: 0
            if (availableQuantity < requiredQuantity) {
                articleId to requiredQuantity - availableQuantity
            } else {
                null
            }
        }.toMap()
    }

    private fun applyInventoryChanges(
        warehouseId: WarehouseId,
        requiredArticlesQuantities: Map<ArticleId, Int>,
    ) {
        val requiredDeltaChanges = requiredArticlesQuantities.mapValues { (_, requiredQuantity) ->
            -requiredQuantity
        }
        articleInventoryService.updateArticleInventory(warehouseId, requiredDeltaChanges)
    }

}