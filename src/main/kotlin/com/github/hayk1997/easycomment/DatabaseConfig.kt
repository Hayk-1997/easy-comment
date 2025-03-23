package com.github.hayk1997.easycomment

import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    fun connect() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/easy_comment",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "secret"
        )
    }
}
