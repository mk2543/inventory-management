package com.gmail.mk2543.inventory.domain.services

import com.gmail.mk2543.inventory.domain.models.ArticleId
import com.gmail.mk2543.inventory.domain.models.SaveArticleDefinitionRequest
import com.gmail.mk2543.inventory.domain.models.WarehouseId

interface ArticleInventoryService {

    fun saveArticles(requests: List<SaveArticleDefinitionRequest>)

    fun findNotExistingIds(articleIds: Set<ArticleId>): Set<ArticleId>

    fun fetchCurrentInventory(warehouseId: WarehouseId, articleIds: Set<ArticleId>): Map<ArticleId, Int>

    fun updateArticleInventory(warehouseId: WarehouseId, inventoryDeltaChanges: Map<ArticleId, Int>)
}