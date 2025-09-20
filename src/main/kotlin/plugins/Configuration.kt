package com.saban.plugins

import com.typesafe.config.ConfigFactory
import io.github.config4k.getValue
import org.koin.core.component.KoinComponent

class SabanConfig : KoinComponent {
    private val config = ConfigFactory.load()

    val database: DatabaseConfig by config
    val s3: S3Config by config
    val security: SecurityConfig by config
}

data class DatabaseConfig(
    val host: String,
    val driver: String,
    val database: String,
    val port: Int,
    val username: String,
    val password: String
)

data class S3Config(
    val keyId: String,
    val secret: String,
    val endpoint: String,
    val protocol: String
)

data class SecurityConfig(
    val adminUser: AdminUser
)

data class AdminUser(
    val email: String,
    val username: String,
    val password: String
)