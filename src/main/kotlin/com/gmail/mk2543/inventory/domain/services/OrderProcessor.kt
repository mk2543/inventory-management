package com.gmail.mk2543.inventory.domain.services

import com.gmail.mk2543.inventory.domain.models.ArticleId
import com.gmail.mk2543.inventory.domain.models.OrderLine
import com.gmail.mk2543.inventory.domain.models.WarehouseId


interface OrderProcessor {

    fun handlePurchase(warehouseId: WarehouseId, orderLines: Set<OrderLine>): Map<ArticleId, Int>
}

