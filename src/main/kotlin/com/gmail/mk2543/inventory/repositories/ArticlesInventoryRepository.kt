package com.gmail.mk2543.inventory.repositories

import com.gmail.mk2543.inventory.domain.models.ArticleId
import com.gmail.mk2543.inventory.domain.models.ArticleInventory
import com.gmail.mk2543.inventory.domain.models.WarehouseId

interface ArticlesInventoryRepository {

    fun findInventoryByArticleIds(warehouseId: WarehouseId, ids: Collection<ArticleId>): Map<ArticleId, ArticleInventory>

    fun save(inventory: ArticleInventory)

    fun saveAll(inventories: Collection<ArticleInventory>)

    fun deleteAll()
}