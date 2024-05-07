package com.gmail.mk2543.inventory.controllers

import com.gmail.mk2543.inventory.domain.models.WarehouseId
import com.gmail.mk2543.inventory.domain.services.OrderProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/v1/warehouses/{warehouseId}/orders")
class OrderProcessorController(private val orderProcessor: OrderProcessor) {

    val logger: Logger = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)

    @PostMapping
    fun processOrder(
        @PathVariable("warehouseId") warehouseId: Long,
        @RequestBody orderLineDto: Set<OrderLineDto>
    ): PurchasedArticlesDto {
        val orderLines = orderLineDto.map { it.toDomain() }.toSet()
        val id = WarehouseId(warehouseId)
        val purchasedArticles = orderProcessor.handlePurchase(id, orderLines)
        logger.info("Order processed: warehouse: $id, order lines: $orderLines, result: $purchasedArticles")
        return PurchasedArticlesDto(purchasedArticles.map { it.toDto() })
    }
}