package com.gmail.mk2543.inventory.domain.services

import com.gmail.mk2543.inventory.domain.models.Product

interface ProductService {

    fun saveProducts(products: List<Product>): List<Product>

}