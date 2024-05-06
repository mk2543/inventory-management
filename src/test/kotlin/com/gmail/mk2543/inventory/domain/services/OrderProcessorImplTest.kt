package com.gmail.mk2543.inventory.domain.services

import com.gmail.mk2543.inventory.Fixtures
import com.gmail.mk2543.inventory.domain.models.ArticlesNotAvailable
import com.gmail.mk2543.inventory.domain.models.OrderLine
import com.gmail.mk2543.inventory.domain.models.ProductsNotFound
import com.gmail.mk2543.inventory.domain.services.impl.OrderProcessorImpl
import com.gmail.mk2543.inventory.repositories.InMemoryProductsRepository
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockkClass
import io.mockk.verify
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class OrderProcessorImplTest {

    private val productsRepository = InMemoryProductsRepository()
    private val articleInventoryService = mockkClass(ArticleInventoryService::class)

    private val articleService: OrderProcessor = OrderProcessorImpl(productsRepository, articleInventoryService)

    @BeforeEach
    fun setup() {
        productsRepository.save(Fixtures.whiteTableProduct)
        productsRepository.save(Fixtures.blackTableProduct)
        productsRepository.save(Fixtures.shelfProduct)
    }

    @AfterEach
    fun cleanUp() {
        productsRepository.deleteAll()
    }

    @Test
    fun `handle purchase should throw an exception when some products are not found`() {
        // given
        productsRepository.save(Fixtures.whiteTableProduct)
        val orderLines = setOf(OrderLine(Fixtures.whiteTableId, 1), OrderLine(Fixtures.notExistingProductId, 1))

        // when
        assertThatThrownBy {
            articleService.handlePurchase(
                Fixtures.warehouseId,
                orderLines
            )
        }.isInstanceOf(ProductsNotFound::class.java)
            .hasMessage("Products not found: [999]")
    }

    @Test
    fun `handle purchase should throw an exception when a single product isn't available`() {
        // given
        every {
            articleInventoryService.fetchCurrentInventory(
                Fixtures.warehouseId,
                setOf(Fixtures.tableLegId, Fixtures.whiteTableSurfaceId)
            )
        } returns mapOf(
            Fixtures.tableLegId to 3,
            Fixtures.whiteTableSurfaceId to 1
        )
        val expectedMissingArticles = mapOf(Fixtures.tableLegId to 1)
        val orderLines = setOf(OrderLine(Fixtures.whiteTableId, 1))

        // when
        val thrown = catchThrowable {
            articleService.handlePurchase(
                Fixtures.warehouseId,
                orderLines
            )
        }

        assertThat(thrown).isInstanceOf(ArticlesNotAvailable::class.java)
        assertThat((thrown as ArticlesNotAvailable).missingArticles).isEqualTo(expectedMissingArticles)
    }

    @Test
    fun `handle purchase should throw an exception when not all products in single order line quantity are available`() {
        // given
        every {
            articleInventoryService.fetchCurrentInventory(
                Fixtures.warehouseId,
                setOf(Fixtures.tableLegId, Fixtures.whiteTableSurfaceId)
            )
        } returns mapOf(
            Fixtures.tableLegId to 4,
            Fixtures.whiteTableSurfaceId to 1
        )
        val expectedMissingArticles = mapOf(Fixtures.tableLegId to 4, Fixtures.whiteTableSurfaceId to 1)
        val orderLines = setOf(OrderLine(Fixtures.whiteTableId, 2))

        // when
        val thrown = catchThrowable {
            articleService.handlePurchase(
                Fixtures.warehouseId,
                orderLines
            )
        }

        assertThat(thrown).isInstanceOf(ArticlesNotAvailable::class.java)
        assertThat((thrown as ArticlesNotAvailable).missingArticles).isEqualTo(expectedMissingArticles)
    }

    @Test
    fun `handle purchase should throw an exception when not all products in multiple order lines are available`() {
        // given
        every { articleInventoryService.fetchCurrentInventory(Fixtures.warehouseId, any()) } returns mapOf(
            Fixtures.tableLegId to 4,
            Fixtures.whiteTableSurfaceId to 1,
            Fixtures.shelfPlankId to 1,
            Fixtures.shelfHangerId to 1
        )
        val expectedMissingArticles = mapOf(Fixtures.shelfHangerId to 1)
        val orderLines = setOf(OrderLine(Fixtures.whiteTableId, 1), OrderLine(Fixtures.shelfId, 1))

        // when
        val thrown = catchThrowable {
            articleService.handlePurchase(
                Fixtures.warehouseId,
                orderLines
            )
        }

        assertThat(thrown).isInstanceOf(ArticlesNotAvailable::class.java)
        assertThat((thrown as ArticlesNotAvailable).missingArticles).isEqualTo(expectedMissingArticles)
    }

    @Test
    fun `handle purchase should throw an exception when not all products with overlapping articles are available`() {
        // given
        every { articleInventoryService.fetchCurrentInventory(Fixtures.warehouseId, any()) } returns mapOf(
            Fixtures.tableLegId to 7,
            Fixtures.whiteTableSurfaceId to 1,
            Fixtures.blackTableSurfaceId to 1
        )
        val orderLines = setOf(OrderLine(Fixtures.whiteTableId, 1), OrderLine(Fixtures.blackTableId, 1))
        val expectedMissingArticles = mapOf(Fixtures.tableLegId to 1)

        // when
        val thrown = catchThrowable {
            articleService.handlePurchase(
                Fixtures.warehouseId,
                orderLines
            )
        }

        assertThat(thrown).isInstanceOf(ArticlesNotAvailable::class.java)
        assertThat((thrown as ArticlesNotAvailable).missingArticles).isEqualTo(expectedMissingArticles)
    }

    @Test
    fun `handle purchase should update inventory when all products are available`() {
        // given
        every { articleInventoryService.fetchCurrentInventory(Fixtures.warehouseId, any()) } returns mapOf(
            Fixtures.tableLegId to 8,
            Fixtures.whiteTableSurfaceId to 1,
            Fixtures.blackTableSurfaceId to 1
        )
        justRun { articleInventoryService.updateArticleInventory(Fixtures.warehouseId, any()) }
        val orderLines = setOf(OrderLine(Fixtures.whiteTableId, 1), OrderLine(Fixtures.blackTableId, 1))

        // when
        articleService.handlePurchase(
            Fixtures.warehouseId,
            orderLines
        )

        // then
        verify {
            articleInventoryService.updateArticleInventory(
                Fixtures.warehouseId,
                mapOf(Fixtures.tableLegId to -8, Fixtures.whiteTableSurfaceId to -1, Fixtures.blackTableSurfaceId to -1)
            )
        }
    }

    @Test
    fun `handle purchase should return required articles quantities when all products are available`() {
        // given
        every { articleInventoryService.fetchCurrentInventory(Fixtures.warehouseId, any()) } returns mapOf(
            Fixtures.tableLegId to 8,
            Fixtures.whiteTableSurfaceId to 1,
            Fixtures.blackTableSurfaceId to 1
        )
        justRun { articleInventoryService.updateArticleInventory(Fixtures.warehouseId, any()) }
        val orderLines = setOf(OrderLine(Fixtures.whiteTableId, 1), OrderLine(Fixtures.blackTableId, 1))

        // when
        val result = articleService.handlePurchase(
            Fixtures.warehouseId,
            orderLines
        )

        // then
        assertThat(result).isEqualTo(
            mapOf(Fixtures.tableLegId to 8, Fixtures.whiteTableSurfaceId to 1, Fixtures.blackTableSurfaceId to 1)
        )
    }
}