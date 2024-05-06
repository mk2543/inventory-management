package com.gmail.mk2543.inventory.repositories

import com.gmail.mk2543.inventory.domain.models.Article
import com.gmail.mk2543.inventory.domain.models.ArticleId

class InMemoryArticlesRepository: ArticlesRepository {

    private val db = mutableMapOf<ArticleId, Article>()

    override fun saveAll(definitions: Collection<Article>) {
        definitions.forEach { db[it.id] = it }
    }

    override fun getBy(ids: Collection<ArticleId>): Map<ArticleId, Article> {
        return db.filterKeys { it in ids }
    }

    override fun get(id: ArticleId): Article? {
        return db[id]
    }

    override fun getAll(): Map<ArticleId, Article> {
        return db
    }

    override fun deleteAll() {
        db.clear()
    }
}