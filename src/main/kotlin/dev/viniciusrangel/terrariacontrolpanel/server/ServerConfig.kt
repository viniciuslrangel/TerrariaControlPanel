package dev.viniciusrangel.terrariacontrolpanel.server

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import java.util.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@ConfigurationProperties("terraria.server")
data class ServerConfig @ConfigurationInject constructor(
        val path: String,
        val port: Int,
        val maxPlayers: Int,
        val password: String,
        val world: String,
        @Min(1L)
        @Max(3L)
        val autocreate: Int,
        val expert: Boolean = false,
        val motd: Optional<String>,
        val seed: Optional<String>
)