package com.gmail.mk2543.inventory.controllers

import com.gmail.mk2543.inventory.testhelpers.AbstractTest
import com.gmail.mk2543.inventory.domain.models.WarehouseId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class WarehouseSyncControllerTests : AbstractTest() {

    @Test
    fun `should sync articles and products`() {
        // given
        val inventoryJson = ClassPathResource("inventory.json").file.readText()
        val productsJson = ClassPathResource("products.json").file.readText()

        // when
        mockMvc.perform(
            patch("/api/v1/warehouses/1/sync-articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventoryJson)
        )
            .andExpect(status().isOk)

        mockMvc.perform(
            patch("/api/v1/warehouses/sync-products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productsJson)
        )
            .andExpect(status().isOk)

        // then
        val productsInDb = productsRepository.getAll()
        assertThat(productsInDb).hasSize(2)

        val articlesInDb = articlesRepository.getAll()
        assertThat(articlesInDb).hasSize(4)

        val inventoryInDb = articlesInventoryRepository.findQuantityByArticleIds(WarehouseId(1), articlesInDb.keys)
        assertThat(inventoryInDb).hasSize(4)
    }
}