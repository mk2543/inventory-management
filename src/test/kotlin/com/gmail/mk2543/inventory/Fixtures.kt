package com.gmail.mk2543.inventory

import com.gmail.mk2543.inventory.domain.models.ArticleId
import com.gmail.mk2543.inventory.domain.models.Product
import com.gmail.mk2543.inventory.domain.models.ProductId
import com.gmail.mk2543.inventory.domain.models.WarehouseId

object Fixtures {

    val warehouseId = WarehouseId(1)
    val anotherWarehouseId = WarehouseId(2)
    val tableLegId = ArticleId(1)
    val whiteTableSurfaceId = ArticleId(2)
    val blackTableSurfaceId = ArticleId(3)
    val shelfPlankId = ArticleId(4)
    val shelfHangerId = ArticleId(5)
    val whiteTableId = ProductId(21)
    val whiteTableName = "White Dinning Table"
    val whiteTableProduct = Product(
        whiteTableId,
        whiteTableName,
        mapOf(
            tableLegId to 4,
            whiteTableSurfaceId to 1
        )
    )
    val blackTableId = ProductId(22)
    val blackTableName = "Black Dinning Table"
    val blackTableProduct = Product(
        blackTableId,
        blackTableName,
        mapOf(
            tableLegId to 4,
            blackTableSurfaceId to 1
        )
    )
    val shelfId = ProductId(23)
    val shelfName = "Wall shelf"
    val shelfProduct = Product(
        shelfId,
        shelfName,
        mapOf(
            shelfPlankId to 1,
            shelfHangerId to 2
        )
    )
    val notExistingProductId = ProductId(999)
}