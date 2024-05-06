package com.gmail.mk2543.inventory.domain.services.impl

import com.gmail.mk2543.inventory.domain.models.*
import com.gmail.mk2543.inventory.domain.services.ArticleInventoryService
import com.gmail.mk2543.inventory.repositories.ArticlesInventoryRepository
import com.gmail.mk2543.inventory.repositories.ArticlesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleInventoryServiceImpl(
    private val articlesRepository: ArticlesRepository,
    private val articlesInventoryRepository: ArticlesInventoryRepository
) : ArticleInventoryService {
    @Transactional
    override fun saveArticles(requests: List<SaveArticleDefinitionRequest>) {
        val articles = requests.map { Article(it.articleId, it.articleName) }
        articlesRepository.saveAll(articles)

        val overridesGroupedByWarehouse =
            requests.flatMap { it.inventoryOverrides.map { entry -> entry.key to (it.articleId to entry.value) } }
                .groupBy({ it.first }, { it.second })
                .mapValues { it.value.toMap() }


        overridesGroupedByWarehouse.forEach {
            overrideArticleInventory(it.key, it.value)
        }
    }

    @Transactional(readOnly = true)
    override fun findNotExistingIds(articleIds: Set<ArticleId>): Set<ArticleId> {
        val existingArticles = articlesRepository.getBy(articleIds)
        return articleIds.minus(existingArticles.keys)
    }

    @Transactional(readOnly = true)
    override fun fetchCurrentInventory(warehouseId: WarehouseId, articleIds: Set<ArticleId>): Map<ArticleId, Int> {
        val stock = articlesInventoryRepository.findQuantityByArticleIds(warehouseId, articleIds)
        return articleIds.associateWith { stock[it]?.quantity ?: 0 }
    }

    @Transactional
    override fun updateArticleInventory(warehouseId: WarehouseId, inventoryDeltaChanges: Map<ArticleId, Int>) {
        val currentInventory = articlesInventoryRepository.findQuantityByArticleIds(warehouseId, inventoryDeltaChanges.keys)
        val inventoryToUpdate = inventoryDeltaChanges.map { (articleId, quantityDelta) ->
            val articleInventory = currentInventory[articleId] ?: ArticleInventory(articleId, warehouseId, 0, 0)
            val newInventory = articleInventory.quantity + quantityDelta
            if (newInventory < 0) {
                throw IllegalArgumentException("Inventory quantity cannot be negative $articleId")
            }
            articleInventory.copy(quantity = newInventory)
        }

        articlesInventoryRepository.saveAll(inventoryToUpdate)
    }

    private fun overrideArticleInventory(warehouseId: WarehouseId, inventoryOverrides: Map<ArticleId, Int>) {
        val currentInventory = articlesInventoryRepository.findQuantityByArticleIds(warehouseId, inventoryOverrides.keys)
        val articleInventoryToUpdate = inventoryOverrides.map { (articleId, newQuantity) ->
            val articleInventory = currentInventory[articleId] ?: ArticleInventory(articleId, warehouseId, 0, 0)
            articleInventory.copy(quantity = newQuantity)
        }

        articlesInventoryRepository.saveAll(articleInventoryToUpdate)
    }

}