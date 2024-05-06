package com.gmail.mk2543.inventory.controllers

import com.gmail.mk2543.inventory.testhelpers.AbstractTestWithInventory
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ProductCatalogueControllerTests : AbstractTestWithInventory() {

    @Test
    fun `should get product catalogue for specified product IDs`() {
        val productNames = listOf("1")

        mockMvc.perform(
            get("/api/v1/warehouses/1/product-catalogue")
                .param("products", *productNames.toTypedArray())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.products", hasSize<Any>(productNames.size)))
            .andExpect(jsonPath("$.products[0].id", `is`("1")))
            .andExpect(jsonPath("$.products[0].name", `is`("Dining Chair")))
            .andExpect(jsonPath("$.products[0].available_quantity", `is`("2")))
    }

    @Test
    fun `should get full product catalogue`() {
        mockMvc.perform(
            get("/api/v1/warehouses/1/product-catalogue/full")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.products", hasSize<Any>(2)))
    }

    @Test
    fun `should return 400 when unknown product IDs are requested`() {
        val productNames = listOf("1", "2", "999")

        // check that the response body contains UnknownProductsDto
        mockMvc.perform(
            get("/api/v1/warehouses/1/product-catalogue")
                .param("products", *productNames.toTypedArray())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.unknown_products", hasSize<Any>(1)))
            .andExpect(jsonPath("$.unknown_products[0]", `is`("999")))
    }
}