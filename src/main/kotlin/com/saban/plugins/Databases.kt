package com.saban.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database

fun configureDatabases(sabanConfig: SabanConfig) {
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

    Database.connect(dataSource)
}
