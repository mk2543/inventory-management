package com.gmail.mk2543.inventory.controllers

import com.gmail.mk2543.inventory.domain.models.WarehouseId
import com.gmail.mk2543.inventory.domain.services.OrderProcessor
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/v1/warehouses/{warehouseId}/orders")
class OrderProcessorController(private val orderProcessor: OrderProcessor) {

    @PostMapping
    fun processOrder(
        @PathVariable("warehouseId") warehouseId: Long,
        @RequestBody orderLineDto: Set<OrderLineDto>
    ): PurchasedArticlesDto {
        val purchasedArticles =
            orderProcessor.handlePurchase(WarehouseId(warehouseId), orderLineDto.map { it.toDomain() }.toSet())

        return PurchasedArticlesDto(purchasedArticles.map { it.toDto() })
    }
}