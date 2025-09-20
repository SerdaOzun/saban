package com.saban.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.koin.ktor.ext.inject

fun Application.configureDatabases() {
    val sabanConfig: SabanConfig by inject()

    val config = HikariConfig().apply {
        sabanConfig.database.let { cfg ->
            jdbcUrl = "jdbc:postgresql://${cfg.host}/${cfg.database}"
            driverClassName = cfg.driver
            username = cfg.username
            password = cfg.password
            maximumPoolSize = 6
            isReadOnly = false
            transactionIsolation = "TRANSACTION_SERIALIZABLE"
        }
    }

    val dataSource = HikariDataSource(config)

    Database.connect(
        datasource = dataSource,
        databaseConfig = DatabaseConfig {}
    )
}
