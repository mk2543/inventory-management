package com.gmail.mk2543.inventory.repositories.postgres

import com.gmail.mk2543.inventory.domain.models.ArticleId
import com.gmail.mk2543.inventory.domain.models.ArticleInventory
import com.gmail.mk2543.inventory.domain.models.WarehouseId
import com.gmail.mk2543.inventory.jooq.tables.references.ARTICLES_INVENTORY
import com.gmail.mk2543.inventory.repositories.ArticlesInventoryRepository
import org.jooq.DSLContext
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class JooqArticlesInventoryRepository(private val dslContext: DSLContext) : ArticlesInventoryRepository {

    @Transactional(readOnly = true)
    override fun findInventoryByArticleIds(
        warehouseId: WarehouseId,
        ids: Collection<ArticleId>
    ): Map<ArticleId, ArticleInventory> {
        val records = dslContext.select(
            ARTICLES_INVENTORY.ARTICLE_ID,
            ARTICLES_INVENTORY.QUANTITY,
            ARTICLES_INVENTORY.VERSION
        )
            .from(ARTICLES_INVENTORY)
            .where(ARTICLES_INVENTORY.ARTICLE_ID.`in`(ids.map { it.value }))
            .and(ARTICLES_INVENTORY.WAREHOUSE_ID.eq(warehouseId.value))
            .fetch()

        return records.associate {
            val articleId = ArticleId(it[ARTICLES_INVENTORY.ARTICLE_ID]!!)
            articleId to ArticleInventory(
                articleId,
                warehouseId,
                it[ARTICLES_INVENTORY.QUANTITY]!!,
                it[ARTICLES_INVENTORY.VERSION]!!
            )
        }
    }

    @Transactional
    override fun save(inventory: ArticleInventory) {
        val updatedRows = dslContext.insertInto(ARTICLES_INVENTORY)
            .set(ARTICLES_INVENTORY.ARTICLE_ID, inventory.articleId.value)
            .set(ARTICLES_INVENTORY.WAREHOUSE_ID, inventory.warehouseId.value)
            .set(ARTICLES_INVENTORY.QUANTITY, inventory.quantity)
            .set(ARTICLES_INVENTORY.VERSION, inventory.version)
            .onDuplicateKeyUpdate()
            .set(ARTICLES_INVENTORY.QUANTITY, inventory.quantity)
            .set(ARTICLES_INVENTORY.VERSION, ARTICLES_INVENTORY.VERSION + 1)
            .where(ARTICLES_INVENTORY.VERSION.eq(inventory.version))
            .execute()

        if (updatedRows == 0) {
            throw OptimisticLockingFailureException("Could not update $inventory due to concurrent modification")
        }
    }

    @Transactional
    override fun saveAll(inventories: Collection<ArticleInventory>) {
        inventories.forEach { save(it) }
    }

    @Transactional
    override fun deleteAll() {
        dslContext.deleteFrom(ARTICLES_INVENTORY).execute()
    }
}