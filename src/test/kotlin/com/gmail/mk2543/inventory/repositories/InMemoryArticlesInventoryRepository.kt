package com.gmail.mk2543.inventory.repositories

import com.gmail.mk2543.inventory.domain.models.ArticleId
import com.gmail.mk2543.inventory.domain.models.ArticleInventory
import com.gmail.mk2543.inventory.domain.models.WarehouseId

class InMemoryArticlesInventoryRepository : ArticlesInventoryRepository {
    private val articleInventoryDb = mutableMapOf<Pair<WarehouseId, ArticleId>, ArticleInventory>()

    override fun findQuantityByArticleIds(warehouseId: WarehouseId, ids: Collection<ArticleId>): Map<ArticleId, ArticleInventory> {
        return articleInventoryDb.filterKeys { it.first == warehouseId && it.second in ids }
            .map { it.key.second to it.value }
            .toMap()
    }

    override fun save(inventory: ArticleInventory) {
        articleInventoryDb[inventory.warehouseId to inventory.articleId] = inventory
    }

    override fun saveAll(inventories: Collection<ArticleInventory>) {
        inventories.forEach { save(it) }
    }

    override fun deleteAll() {
        articleInventoryDb.clear()
    }
}