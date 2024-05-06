package com.gmail.mk2543.inventory.testhelpers

import com.gmail.mk2543.inventory.WarehouseApplication
import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.with
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestWarehouseApplication {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> {
        return PostgreSQLContainer(DockerImageName.parse("postgres:15.6"))
    }

}

fun main(args: Array<String>) {
    fromApplication<WarehouseApplication>().with(TestWarehouseApplication::class).run(*args)
}
