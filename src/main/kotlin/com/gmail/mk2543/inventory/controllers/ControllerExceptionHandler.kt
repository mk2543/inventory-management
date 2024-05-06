package com.gmail.mk2543.inventory.controllers

import com.gmail.mk2543.inventory.domain.models.ArticlesNotAvailable
import com.gmail.mk2543.inventory.domain.models.ProductsNotFound
import com.gmail.mk2543.inventory.domain.models.UnknownArticleException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ControllerExceptionHandler {

    @ExceptionHandler(ProductsNotFound::class)
    fun handleProductsNotFound(exception: ProductsNotFound): ResponseEntity<UnknownProductsDto> {
        val dto = UnknownProductsDto(
            exception.productIds.map { it.value.toString() }
        )
        return ResponseEntity(dto, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ArticlesNotAvailable::class)
    fun handleArticlesNotAvailable(exception: ArticlesNotAvailable): ResponseEntity<MissingArticlesDto> {
        val dto = MissingArticlesDto(
            exception.missingArticles.map { (articleId, amount) ->
                ArticleQuantityDto(articleId.value.toString(), amount.toString())
            }
        )
        return ResponseEntity(dto, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(UnknownArticleException::class)
    fun handleUnknownArticleException(exception: UnknownArticleException): ResponseEntity<UnknownArticlesDto> {
        val dto = UnknownArticlesDto(
            exception.unknownArticles.map { it.value.toString() }
        )
        return ResponseEntity(dto, HttpStatus.BAD_REQUEST)
    }
}