package com.gmail.mk2543.inventory.controllers

import com.gmail.mk2543.inventory.domain.models.WarehouseId
import com.gmail.mk2543.inventory.domain.services.ArticleInventoryService
import com.gmail.mk2543.inventory.domain.services.ProductService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/warehouses")
class WarehouseSyncController(
    private val productService: ProductService,
    private val inventoryService: ArticleInventoryService
) {

    val logger: Logger = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)

    @PatchMapping("/sync-products")
    fun uploadWarehouseProducts(@RequestBody productsDto: ProductsDto): ProductsDto {
        val productsWithId = productService.saveProducts(productsDto.products.map { it.toDomain() })
        logger.info("Synced ${productsWithId.size} products")
        return ProductsDto(productsWithId.map { it.toDto() })
    }

    @PatchMapping("{warehouseId}/sync-articles")
    fun uploadWarehouseArticles(
        @PathVariable("warehouseId") warehouseId: Long,
        @RequestBody inventoryDto: InventoryDto
    ) {
        val id = WarehouseId(warehouseId)
        inventoryService.saveArticles(inventoryDto.inventory.map { it.toDomain(id) })
        logger.info("Synced ${inventoryDto.inventory.size} articles for warehouse $id")
    }
}