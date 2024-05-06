package com.gmail.mk2543.inventory.repositories.postgres

import com.gmail.mk2543.inventory.domain.models.ArticleId
import com.gmail.mk2543.inventory.domain.models.Product
import com.gmail.mk2543.inventory.domain.models.ProductId
import com.gmail.mk2543.inventory.jooq.tables.records.ProductArticlesRecord
import com.gmail.mk2543.inventory.jooq.tables.references.PRODUCTS
import com.gmail.mk2543.inventory.jooq.tables.references.PRODUCT_ARTICLES
import com.gmail.mk2543.inventory.repositories.ProductsRepository
import org.jooq.DSLContext
import org.jooq.Result
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class JooqProductsRepository(private val dslContext: DSLContext) : ProductsRepository {

    @Transactional(readOnly = true)
    override fun get(id: ProductId): Product? {
        val records = dslContext.select(PRODUCTS.asterisk(), PRODUCT_ARTICLES.ARTICLE_ID, PRODUCT_ARTICLES.QUANTITY)
            .from(PRODUCTS)
            .join(PRODUCT_ARTICLES).on(PRODUCTS.ID.eq(PRODUCT_ARTICLES.PRODUCT_ID))
            .where(PRODUCTS.ID.eq(id.value))
            .fetch()

        return mapToProductDefinition(records).firstOrNull()
    }

    @Transactional(readOnly = true)
    override fun getAll(): List<Product> {
        val records = dslContext.select(PRODUCTS.asterisk(), PRODUCT_ARTICLES.ARTICLE_ID, PRODUCT_ARTICLES.QUANTITY)
            .from(PRODUCTS)
            .join(PRODUCT_ARTICLES).on(PRODUCTS.ID.eq(PRODUCT_ARTICLES.PRODUCT_ID))
            .fetch()

        return mapToProductDefinition(records)
    }

    @Transactional(readOnly = true)
    override fun findProductsByIds(ids: Collection<ProductId>): Map<ProductId, Product> {
        val records = dslContext.select(PRODUCTS.asterisk(), PRODUCT_ARTICLES.ARTICLE_ID, PRODUCT_ARTICLES.QUANTITY)
            .from(PRODUCTS)
            .join(PRODUCT_ARTICLES).on(PRODUCTS.ID.eq(PRODUCT_ARTICLES.PRODUCT_ID))
            .where(PRODUCTS.ID.`in`(ids.map { it.value }))
            .fetch()

        return mapToProductDefinition(records).associateBy { it.id!! }
    }

    @Transactional
    override fun save(product: Product): Product {
        val productId = product.id?.value ?: dslContext.nextval("products_id_seq").toLong()
        dslContext.insertInto(PRODUCTS, PRODUCTS.ID, PRODUCTS.NAME)
            .values(productId, product.name)
            .onDuplicateKeyUpdate()
            .set(PRODUCTS.NAME, product.name)
            .execute()

        dslContext.deleteFrom(PRODUCT_ARTICLES)
            .where(PRODUCT_ARTICLES.PRODUCT_ID.eq(productId))
            .execute()

        val productArticles = product.articles.map { (articleId, quantity) ->
            ProductArticlesRecord(productId, articleId.value, quantity)
        }
        dslContext.batchInsert(productArticles).execute()

        return product.copy(id = ProductId(productId))
    }

    @Transactional
    override fun saveAll(products: List<Product>): List<Product> {
        return products.map { save(it) }
    }

    @Transactional
    override fun deleteAll() {
        dslContext.deleteFrom(PRODUCTS).execute()
    }

    private fun mapToProductDefinition(result: Result<*>): List<Product> {
        return result.groupBy { record -> ProductId(record.get(PRODUCTS.ID)!!) to record.get(PRODUCTS.NAME) }
            .map { (productIdAndName, records) ->
                val articles = records.associate { record ->
                    ArticleId(record.get(PRODUCT_ARTICLES.ARTICLE_ID)!!) to record.get(PRODUCT_ARTICLES.QUANTITY)!!
                }
                Product(
                    id = productIdAndName.first,
                    name = productIdAndName.second!!,
                    articles = articles
                )
            }
    }
}
