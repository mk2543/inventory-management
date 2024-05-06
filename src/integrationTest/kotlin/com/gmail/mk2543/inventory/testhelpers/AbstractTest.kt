package com.gmail.mk2543.inventory.testhelpers

import com.gmail.mk2543.inventory.repositories.ArticlesInventoryRepository
import com.gmail.mk2543.inventory.repositories.ArticlesRepository
import com.gmail.mk2543.inventory.repositories.ProductsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@Import(TestWarehouseApplication::class)
@AutoConfigureMockMvc
abstract class AbstractTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var articlesInventoryRepository: ArticlesInventoryRepository

    @Autowired
    lateinit var articlesRepository: ArticlesRepository

    @Autowired
    lateinit var productsRepository: ProductsRepository

}