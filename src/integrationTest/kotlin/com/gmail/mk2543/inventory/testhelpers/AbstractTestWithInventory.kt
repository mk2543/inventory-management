package com.gmail.mk2543.inventory.testhelpers

import com.gmail.mk2543.inventory.domain.models.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class AbstractTestWithInventory : AbstractTest() {

    @BeforeEach
    fun setUp() {
        articlesRepository.saveAll(
            // save article definitions matching inventory.json
            listOf(
                Article(ArticleId(1), "leg"),
                Article(ArticleId(2), "screw"),
                Article(ArticleId(3), "seat"),
                Article(ArticleId(4), "table top"),
                Article(ArticleId(5), "table leg"),
                Article(ArticleId(6), "table screw")
            )
        )

        val warehouseId = WarehouseId(1)
        articlesInventoryRepository.saveAll(
            // save article stock matching inventory.json
            listOf(
                ArticleInventory(ArticleId(1), warehouseId, 12, 1),
                ArticleInventory(ArticleId(2), warehouseId, 17, 1),
                ArticleInventory(ArticleId(3), warehouseId, 2, 1),
                ArticleInventory(ArticleId(4), warehouseId, 1, 1),
                ArticleInventory(ArticleId(5), warehouseId, 4, 1),
                ArticleInventory(ArticleId(6), warehouseId, 8, 1)
            )
        )

        productsRepository.saveAll(
            // save product definitions matching products.json
            listOf(
                Product(
                    ProductId(1),
                    "Dining Chair",
                    mapOf(
                        ArticleId(1) to 4,
                        ArticleId(2) to 8,
                        ArticleId(3) to 1,
                    )
                ),
                Product(
                    ProductId(2),
                    "Dining Table",
                    mapOf(
                        ArticleId(4) to 1,
                        ArticleId(5) to 4,
                        ArticleId(6) to 4,
                    )
                )
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        productsRepository.deleteAll()
        articlesInventoryRepository.deleteAll()
        articlesRepository.deleteAll()
    }
}