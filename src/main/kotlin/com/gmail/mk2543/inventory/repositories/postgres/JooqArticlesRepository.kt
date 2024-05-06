package com.gmail.mk2543.inventory.repositories.postgres

import com.gmail.mk2543.inventory.domain.models.Article
import com.gmail.mk2543.inventory.domain.models.ArticleId
import com.gmail.mk2543.inventory.jooq.tables.records.ArticlesRecord
import com.gmail.mk2543.inventory.jooq.tables.references.ARTICLES
import com.gmail.mk2543.inventory.repositories.ArticlesRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class JooqArticlesRepository(private val dslContext: DSLContext) : ArticlesRepository {

    @Transactional
    override fun saveAll(definitions: Collection<Article>) {
        val records = definitions.map { definition ->
            ArticlesRecord().apply {
                id = definition.id.value
                name = definition.name
            }
        }

        dslContext.batchInsert(records).execute()
    }

    @Transactional(readOnly = true)
    override fun get(id: ArticleId): Article? {
        return dslContext.selectFrom(ARTICLES)
            .where(ARTICLES.ID.eq(id.value))
            .fetchOne()?.toDomain()
    }

    @Transactional(readOnly = true)
    override fun getAll(): Map<ArticleId, Article> {
        return dslContext.selectFrom(ARTICLES)
            .fetch()
            .associate { ArticleId(it.id!!) to it.toDomain()}
    }

    @Transactional(readOnly = true)
    override fun getBy(ids: Collection<ArticleId>): Map<ArticleId, Article> {
        return dslContext.selectFrom(ARTICLES)
            .where(ARTICLES.ID.`in`(ids.map { it.value }))
            .fetch()
            .associate { ArticleId(it.id!!) to it.toDomain() }
    }

    @Transactional
    override fun deleteAll() {
        dslContext.deleteFrom(ARTICLES).execute()
    }

    fun ArticlesRecord.toDomain() = Article(
        id = ArticleId(id!!),
        name = name!!
    )
}

