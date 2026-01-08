package com.saban.plugins

import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.koin.ktor.ext.inject

fun Application.configureMigrations() {
    val sabanConfig: SabanConfig by inject()
    val cfg = sabanConfig.database

    val flyway =
        Flyway.configure().dataSource("jdbc:postgresql://${cfg.host}/${cfg.database}", cfg.username, cfg.password)
            .baselineOnMigrate(true)
            .load()

    flyway.migrate()
}
