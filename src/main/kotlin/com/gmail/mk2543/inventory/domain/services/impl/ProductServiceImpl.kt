package com.gmail.mk2543.inventory.domain.services.impl

import com.gmail.mk2543.inventory.domain.models.Product
import com.gmail.mk2543.inventory.domain.models.UnknownArticleException
import com.gmail.mk2543.inventory.domain.services.ArticleInventoryService
import com.gmail.mk2543.inventory.domain.services.ProductService
import com.gmail.mk2543.inventory.repositories.ProductsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductServiceImpl(
    private val productsRepository: ProductsRepository,
    private val articleInventoryService: ArticleInventoryService
) : ProductService {
    @Transactional
    override fun saveProducts(products: List<Product>): List<Product> {
        val requiredArticleIds = products.map { it.articles.keys }.flatten().toSet()

        val notExistingArticleIds = articleInventoryService.findNotExistingIds(requiredArticleIds)
        if (notExistingArticleIds.isNotEmpty()) {
            throw UnknownArticleException(notExistingArticleIds)
        }

        return productsRepository.saveAll(products)
    }
}