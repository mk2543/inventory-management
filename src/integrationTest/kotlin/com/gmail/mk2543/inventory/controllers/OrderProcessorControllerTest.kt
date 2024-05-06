package com.gmail.mk2543.inventory.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.gmail.mk2543.inventory.testhelpers.AbstractTestWithInventory
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class OrderProcessorControllerTest : AbstractTestWithInventory() {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `process order should return purchased articles`() {
        val orderLineDto = setOf(OrderLineDto("1", "1"))
        val orderLineDtoJson = objectMapper.writeValueAsString(orderLineDto)

        mockMvc.perform(
            post("/api/v1/warehouses/1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderLineDtoJson)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.articles", hasSize<Any>(3)))
            .andExpect(jsonPath("$.articles[0].art_id", `is`("1")))
            .andExpect(jsonPath("$.articles[0].amount_of", `is`("4")))
            .andExpect(jsonPath("$.articles[1].art_id", `is`("2")))
            .andExpect(jsonPath("$.articles[1].amount_of", `is`("8")))
            .andExpect(jsonPath("$.articles[2].art_id", `is`("3")))
            .andExpect(jsonPath("$.articles[2].amount_of", `is`("1")))
    }

    @Test
    fun `process order should return 400 when unknown product IDs are requested`() {
        val orderLineDto = setOf(OrderLineDto("1", "1"), OrderLineDto("2", "1"), OrderLineDto("999", "1"))
        val orderLineDtoJson = objectMapper.writeValueAsString(orderLineDto)

        mockMvc.perform(
            post("/api/v1/warehouses/1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderLineDtoJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.unknown_products", hasSize<Any>(1)))
            .andExpect(jsonPath("$.unknown_products[0]", `is`("999")))
    }

    @Test
    fun `process order should return 409 when insufficient stock is available`() {
        val orderLineDto = setOf(OrderLineDto("1", "100"))
        val orderLineDtoJson = objectMapper.writeValueAsString(orderLineDto)

        mockMvc.perform(
            post("/api/v1/warehouses/1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderLineDtoJson)
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.missing_articles", hasSize<Any>(3)))
            .andExpect(jsonPath("$.missing_articles[0].art_id", `is`("1")))
            .andExpect(jsonPath("$.missing_articles[0].amount_of", `is`("388")))
    }
}