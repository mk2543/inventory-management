package com.gmail.mk2543.inventory.domain.models


class ProductsNotFound(val productIds: Collection<ProductId>) :
    RuntimeException("Products not found: ${productIds.map { it.value }}")

class ArticlesNotAvailable(val missingArticles: Map<ArticleId, Int>) :
    RuntimeException("Missing articles: ${missingArticles.map { "${it.key} x${it.value}" }}")

class UnknownArticleException(val unknownArticles: Collection<ArticleId>) :
    RuntimeException("Unknown articles: ${unknownArticles.map { it.value }}")