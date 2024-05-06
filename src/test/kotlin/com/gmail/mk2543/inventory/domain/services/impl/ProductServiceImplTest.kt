package com.gmail.mk2543.inventory.domain.services.impl

import com.gmail.mk2543.inventory.Fixtures
import com.gmail.mk2543.inventory.domain.models.UnknownArticleException
import com.gmail.mk2543.inventory.domain.services.ArticleInventoryService
import com.gmail.mk2543.inventory.repositories.InMemoryProductsRepository
import io.mockk.every
import io.mockk.mockkClass
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductServiceImplTest {
    private val articleInventoryService: ArticleInventoryService = mockkClass(ArticleInventoryService::class)
    private val productsRepository = InMemoryProductsRepository()

    private val productsService = ProductServiceImpl(productsRepository, articleInventoryService)

    @Test
    fun `saveProducts should save products when all articles exist`() {
        // given
        val products = listOf(Fixtures.whiteTableProduct, Fixtures.blackTableProduct, Fixtures.shelfProduct)
        every { articleInventoryService.findNotExistingIds(any()) } returns emptySet()


        // when
        val result = productsService.saveProducts(products)

        // then
        val productsInDb = productsRepository.getAll()
        assertThat(result).isEqualTo(products)
        assertThat(products).isEqualTo(productsInDb)
    }

    @Test
    fun `saveProducts should throw UnknownArticleException when some articles do not exist`() {
        // given
        val products = listOf(Fixtures.whiteTableProduct, Fixtures.blackTableProduct, Fixtures.shelfProduct)
        every { articleInventoryService.findNotExistingIds(any()) } returns setOf(Fixtures.tableLegId)

        // then
        assertThrows<UnknownArticleException> {
            productsService.saveProducts(products)
        }
    }
}