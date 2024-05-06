package com.gmail.mk2543.inventory.controllers

import com.gmail.mk2543.inventory.domain.models.ProductId
import com.gmail.mk2543.inventory.domain.models.WarehouseId
import com.gmail.mk2543.inventory.domain.services.ProductCatalogueService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/warehouses/{warehouseId}/product-catalogue")
class ProductCatalogueController(private val productCatalogueService: ProductCatalogueService) {

    @GetMapping
    fun getProductCatalogue(@PathVariable("warehouseId") warehouseId: Long, @RequestParam products: List<String>): CatalogueDto {
        val catalogue =
            productCatalogueService.fetchProductsBy(WarehouseId(warehouseId), products.map { ProductId(it.toLong()) })
        return CatalogueDto(catalogue.map { it.toDto() })
    }

    @GetMapping("/full")
    fun getFullProductCatalogue(@PathVariable("warehouseId") warehouseId: Long): CatalogueDto {
        val catalogue = productCatalogueService.fetchAllProductsBy(WarehouseId(warehouseId))
        return CatalogueDto(catalogue.map { it.toDto() })
    }
}
