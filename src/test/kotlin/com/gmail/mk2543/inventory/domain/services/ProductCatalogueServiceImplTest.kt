package com.gmail.mk2543.inventory.domain.services

import com.gmail.mk2543.inventory.Fixtures
import com.gmail.mk2543.inventory.domain.models.CatalogueProduct
import com.gmail.mk2543.inventory.domain.models.ProductsNotFound
import com.gmail.mk2543.inventory.domain.services.impl.ProductCatalogueServiceImpl
import com.gmail.mk2543.inventory.repositories.InMemoryProductsRepository
import io.mockk.every
import io.mockk.mockkClass
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProductCatalogueServiceImplTest {

    private val articleInventoryService: ArticleInventoryService = mockkClass(ArticleInventoryService::class)
    private val productsRepository = InMemoryProductsRepository()

    private val productCatalogueService = ProductCatalogueServiceImpl(productsRepository, articleInventoryService)

    @BeforeEach
    fun setup() {
        productsRepository.save(Fixtures.whiteTableProduct)
        productsRepository.save(Fixtures.blackTableProduct)
        productsRepository.save(Fixtures.shelfProduct)
    }

    @Test
    fun `fetchProductsBy should throw exception when product is not found`() {
        // when
        val thrown = assertThrows(ProductsNotFound::class.java) {
            productCatalogueService.fetchProductsBy(Fixtures.warehouseId, listOf(Fixtures.notExistingProductId))
        }

        // then
        assertEquals("Products not found: [999]", thrown.message)
    }

    @Test
    fun `fetchProductsBy should return catalogue products for given product ids`() {
        // given
        every { articleInventoryService.fetchCurrentInventory(Fixtures.warehouseId, any()) } returns mapOf(
            Fixtures.tableLegId to 4,
            Fixtures.whiteTableSurfaceId to 1
        )

        val expectedCatalogueProduct = CatalogueProduct(Fixtures.whiteTableId, Fixtures.whiteTableName, 1)

        // when
        val products = productCatalogueService.fetchProductsBy(Fixtures.warehouseId, listOf(Fixtures.whiteTableId))

        // then
        assertThat(products).containsExactly(expectedCatalogueProduct)
    }

    @Test
    fun `fetchProductsBy should calculate availability based on article inventory`() {
        // given
        every { articleInventoryService.fetchCurrentInventory(Fixtures.warehouseId, any()) } returns mapOf(
            Fixtures.tableLegId to 8,
            Fixtures.whiteTableSurfaceId to 3
        )

        val expectedCatalogueProduct = CatalogueProduct(Fixtures.whiteTableId, Fixtures.whiteTableName, 2)

        // when
        val products = productCatalogueService.fetchProductsBy(Fixtures.warehouseId, listOf(Fixtures.whiteTableId))

        // then

        assertThat(products).containsExactly(expectedCatalogueProduct)
    }

    @Test
    fun `fetchProductsBy should count the same article inventory for multiple products`() {
        // given
        every { articleInventoryService.fetchCurrentInventory(Fixtures.warehouseId, any()) } returns mapOf(
            Fixtures.tableLegId to 4,
            Fixtures.whiteTableSurfaceId to 1,
            Fixtures.blackTableSurfaceId to 1
        )

        // when
        val products = productCatalogueService.fetchProductsBy(
            Fixtures.warehouseId,
            listOf(Fixtures.whiteTableId, Fixtures.blackTableId)
        )

        // then
        assertThat(products).containsExactlyInAnyOrder(
            CatalogueProduct(
                Fixtures.whiteTableId,
                Fixtures.whiteTableName,
                1
            ), CatalogueProduct(Fixtures.blackTableId, Fixtures.blackTableName, 1)
        )
    }

    @Test
    fun `fetchProductsBy should handle unavailable products`() {
        // given
        every { articleInventoryService.fetchCurrentInventory(Fixtures.warehouseId, any()) } returns mapOf(
            Fixtures.tableLegId to 0,
            Fixtures.whiteTableSurfaceId to 0,
            Fixtures.blackTableSurfaceId to 0
        )

        val expectedWhiteTableCatalogueProduct = CatalogueProduct(Fixtures.whiteTableId, Fixtures.whiteTableName, 0)
        val expectedBlackTableCatalogueProduct = CatalogueProduct(Fixtures.blackTableId, Fixtures.blackTableName, 0)

        // when
        val products = productCatalogueService.fetchProductsBy(
            Fixtures.warehouseId,
            listOf(Fixtures.whiteTableId, Fixtures.blackTableId)
        )

        // then
        assertThat(products).containsExactlyInAnyOrder(
            expectedWhiteTableCatalogueProduct, expectedBlackTableCatalogueProduct
        )
    }

    @Test
    fun `fetchAllProductsBy should return all catalogue products`() {
        // given
        every { articleInventoryService.fetchCurrentInventory(Fixtures.warehouseId, any()) } returns mapOf(
            Fixtures.tableLegId to 4,
            Fixtures.whiteTableSurfaceId to 1,
            Fixtures.blackTableSurfaceId to 1,
            Fixtures.shelfPlankId to 2,
            Fixtures.shelfHangerId to 4
        )

        // when
        val products = productCatalogueService.fetchAllProductsBy(Fixtures.warehouseId)

        // then
        assertThat(products).containsExactlyInAnyOrder(
            CatalogueProduct(Fixtures.whiteTableId, Fixtures.whiteTableName, 1),
            CatalogueProduct(Fixtures.blackTableId, Fixtures.blackTableName, 1),
            CatalogueProduct(Fixtures.shelfId, Fixtures.shelfName, 2)
        )
    }
}