package com.gmail.mk2543.inventory.domain.services

import com.gmail.mk2543.inventory.Fixtures
import com.gmail.mk2543.inventory.domain.models.Article
import com.gmail.mk2543.inventory.domain.models.ArticleInventory
import com.gmail.mk2543.inventory.domain.models.SaveArticleDefinitionRequest
import com.gmail.mk2543.inventory.domain.services.impl.ArticleInventoryServiceImpl
import com.gmail.mk2543.inventory.repositories.InMemoryArticlesInventoryRepository
import com.gmail.mk2543.inventory.repositories.InMemoryArticlesRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class ArticleInventoryServiceImplTest {

    private val articlesRepository = InMemoryArticlesRepository()
    private val articleInventoryRepository = InMemoryArticlesInventoryRepository()

    private val articleInventoryService =
        ArticleInventoryServiceImpl(articlesRepository, articleInventoryRepository)

    @AfterEach
    fun cleanUp() {
        articlesRepository.deleteAll()
        articleInventoryRepository.deleteAll()
    }

    @Test
    fun `saveArticles should save given articles`() {
        // given
        val requests = listOf(
            SaveArticleDefinitionRequest(
                articleId = Fixtures.tableLegId,
                articleName = "Table leg",
                inventoryOverrides = mapOf(Fixtures.warehouseId to 10, Fixtures.anotherWarehouseId to 20)
            ),
            SaveArticleDefinitionRequest(
                articleId = Fixtures.whiteTableSurfaceId,
                articleName = "White table surface",
                inventoryOverrides = mapOf(Fixtures.warehouseId to 5, Fixtures.anotherWarehouseId to 15)
            )
        )
        val expectedTableLegDefinition = Article(Fixtures.tableLegId, "Table leg")
        val expectedTableSurfaceDefinition = Article(Fixtures.whiteTableSurfaceId, "White table surface")

        // when
        articleInventoryService.saveArticles(requests)

        // then
        val tableLegDefinition = articlesRepository.get(Fixtures.tableLegId)
        val tableSurfaceDefinition = articlesRepository.get(Fixtures.whiteTableSurfaceId)

        assertThat(tableLegDefinition).isEqualTo(expectedTableLegDefinition)
        assertThat(tableSurfaceDefinition).isEqualTo(expectedTableSurfaceDefinition)
    }

    @Test
    fun `saveArticles should insert quantities for inventory that wasn't tracked before`() {
        // given
        val requests = listOf(
            SaveArticleDefinitionRequest(
                articleId = Fixtures.tableLegId,
                articleName = "Table leg",
                inventoryOverrides = mapOf(Fixtures.warehouseId to 10)
            ),
            SaveArticleDefinitionRequest(
                articleId = Fixtures.whiteTableSurfaceId,
                articleName = "White table surface",
                inventoryOverrides = mapOf(Fixtures.warehouseId to 5)
            )
        )

        // when
        articleInventoryService.saveArticles(requests)

        // then
        val currentInventory = articleInventoryRepository.findInventoryByArticleIds(
            Fixtures.warehouseId,
            setOf(Fixtures.tableLegId, Fixtures.whiteTableSurfaceId)
        )

        assertThat(currentInventory[Fixtures.tableLegId]?.quantity).isEqualTo(10)
        assertThat(currentInventory[Fixtures.whiteTableSurfaceId]?.quantity).isEqualTo(5)
    }

    @Test
    fun `saveArticles should insert quantities into multiple warehouses`() {
        // given
        val requests = listOf(
            SaveArticleDefinitionRequest(
                articleId = Fixtures.tableLegId,
                articleName = "Table leg",
                inventoryOverrides = mapOf(Fixtures.warehouseId to 10, Fixtures.anotherWarehouseId to 20)
            ),
        )

        // when
        articleInventoryService.saveArticles(requests)

        // then
        val currentInventoryInFirstWarehouse =
            articleInventoryRepository.findInventoryByArticleIds(Fixtures.warehouseId, setOf(Fixtures.tableLegId))
        val currentInventoryInSecondWarehouse =
            articleInventoryRepository.findInventoryByArticleIds(Fixtures.anotherWarehouseId, setOf(Fixtures.tableLegId))

        assertThat(currentInventoryInFirstWarehouse[Fixtures.tableLegId]?.quantity).isEqualTo(10)
        assertThat(currentInventoryInSecondWarehouse[Fixtures.tableLegId]?.quantity).isEqualTo(20)
    }

    @Test
    fun `saveArticles should override quantities for inventory that was already tracked in a warehouse`() {
        // given
        articlesRepository.saveAll(listOf(Article(Fixtures.tableLegId, "Table leg")))
        articleInventoryRepository.save(ArticleInventory(Fixtures.tableLegId, Fixtures.warehouseId, 7, 1))

        val requests = listOf(
            SaveArticleDefinitionRequest(
                articleId = Fixtures.tableLegId,
                articleName = "Table leg",
                inventoryOverrides = mapOf(Fixtures.warehouseId to 10)
            ),
        )

        // when
        articleInventoryService.saveArticles(requests)

        // then
        val currentInventory =
            articleInventoryRepository.findInventoryByArticleIds(Fixtures.warehouseId, setOf(Fixtures.tableLegId))

        assertThat(currentInventory[Fixtures.tableLegId]?.quantity).isEqualTo(10)
    }

    @Test
    fun `updateArticleInventory should update inventory for a warehouse`() {
        // given
        articleInventoryRepository.save(ArticleInventory(Fixtures.tableLegId, Fixtures.warehouseId, 7, 1))
        articleInventoryRepository.save(ArticleInventory(Fixtures.whiteTableSurfaceId, Fixtures.warehouseId, 1, 1))

        val inventoryDeltaChanges = mapOf(Fixtures.tableLegId to 3, Fixtures.whiteTableSurfaceId to 2)

        // when
        articleInventoryService.updateArticleInventory(Fixtures.warehouseId, inventoryDeltaChanges)

        // then
        val currentInventory = articleInventoryRepository.findInventoryByArticleIds(
            Fixtures.warehouseId,
            setOf(Fixtures.tableLegId, Fixtures.whiteTableSurfaceId)
        )

        assertThat(currentInventory[Fixtures.tableLegId]?.quantity).isEqualTo(10)
        assertThat(currentInventory[Fixtures.whiteTableSurfaceId]?.quantity).isEqualTo(3)
    }

    @Test
    fun `updateArticleInventory should create new entry when inventory wasn't tracked before`() {
        // given
        val inventoryDeltaChanges = mapOf(Fixtures.tableLegId to 3)

        // when
        articleInventoryService.updateArticleInventory(Fixtures.warehouseId, inventoryDeltaChanges)

        // then
        val currentInventory = articleInventoryRepository.findInventoryByArticleIds(Fixtures.warehouseId, setOf(Fixtures.tableLegId))

        assertThat(currentInventory[Fixtures.tableLegId]?.quantity).isEqualTo(3)
    }

    @Test
    fun `updateArticleInventory should throw an exception when inventory would go negative`() {
        // given
        articleInventoryRepository.save(ArticleInventory(Fixtures.tableLegId, Fixtures.warehouseId, 7, 1))
        val inventoryDeltaChanges = mapOf(Fixtures.tableLegId to -8)

        // when
        assertThatThrownBy {
            articleInventoryService.updateArticleInventory(Fixtures.warehouseId, inventoryDeltaChanges)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `fetchCurrentInventory should return current inventory for a warehouse`() {
        // given
        articleInventoryRepository.save(ArticleInventory(Fixtures.tableLegId, Fixtures.warehouseId, 7, 1))
        articleInventoryRepository.save(ArticleInventory(Fixtures.whiteTableSurfaceId, Fixtures.warehouseId, 1, 1))

        // when
        val currentInventory = articleInventoryService.fetchCurrentInventory(
            Fixtures.warehouseId,
            setOf(Fixtures.tableLegId, Fixtures.whiteTableSurfaceId)
        )

        // then
        assertThat(currentInventory).containsExactlyInAnyOrderEntriesOf(
            mapOf(Fixtures.tableLegId to 7, Fixtures.whiteTableSurfaceId to 1)
        )
    }

    @Test
    fun `fetchCurrentInventory should return 0 for articles that are not tracked`() {
        // given
        articleInventoryRepository.save(ArticleInventory(Fixtures.tableLegId, Fixtures.warehouseId, 7, 1))

        // when
        val currentInventory = articleInventoryService.fetchCurrentInventory(
            Fixtures.warehouseId,
            setOf(Fixtures.tableLegId, Fixtures.whiteTableSurfaceId)
        )

        // then
        assertThat(currentInventory).containsExactlyInAnyOrderEntriesOf(
            mapOf(Fixtures.tableLegId to 7, Fixtures.whiteTableSurfaceId to 0)
        )
    }

    @Test
    fun `findNotExistingIds should return article ids that are not defined`() {
        // given
        articlesRepository.saveAll(listOf(Article(Fixtures.tableLegId, "Table leg")))

        // when
        val notExistingIds = articleInventoryService.findNotExistingIds(setOf(Fixtures.tableLegId, Fixtures.whiteTableSurfaceId))

        // then
        assertThat(notExistingIds).containsExactly(Fixtures.whiteTableSurfaceId)
    }

    @Test
    fun `findNotExistingIds should return empty set when all articles exist`() {
        // given
        articlesRepository.saveAll(listOf(Article(Fixtures.tableLegId, "Table leg")))

        // when
        val notExistingIds = articleInventoryService.findNotExistingIds(setOf(Fixtures.tableLegId))

        // then
        assertThat(notExistingIds).isEmpty()
    }
}