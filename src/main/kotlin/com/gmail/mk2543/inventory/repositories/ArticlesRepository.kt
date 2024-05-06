package com.gmail.mk2543.inventory.repositories

import com.gmail.mk2543.inventory.domain.models.Article
import com.gmail.mk2543.inventory.domain.models.ArticleId

interface ArticlesRepository {
    fun saveAll(definitions: Collection<Article>)

    fun get(id: ArticleId): Article?

    fun getAll(): Map<ArticleId, Article>

    fun getBy(ids: Collection<ArticleId>): Map<ArticleId, Article>

    fun deleteAll()
}