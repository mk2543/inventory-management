package com.gmail.mk2543.inventory.configuration

import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DefaultConfiguration
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer
import org.springframework.stereotype.Component

@Component
class JooqCustomizer: DefaultConfigurationCustomizer {

    override fun customize(configuration: DefaultConfiguration) {
        val settings = Settings()
            .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
            .withRenderNameCase(RenderNameCase.LOWER_IF_UNQUOTED)

        configuration.set(settings)
    }
}