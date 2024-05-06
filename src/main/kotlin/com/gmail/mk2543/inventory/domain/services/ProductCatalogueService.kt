package com.gmail.mk2543.inventory.domain.services

import com.gmail.mk2543.inventory.domain.models.CatalogueProduct
import com.gmail.mk2543.inventory.domain.models.ProductId
import com.gmail.mk2543.inventory.domain.models.WarehouseId

interface ProductCatalogueService {

    fun fetchProductsBy(warehouseId: WarehouseId, productIds: List<ProductId>): List<CatalogueProduct>

    fun fetchAllProductsBy(warehouseId: WarehouseId): List<CatalogueProduct>
}