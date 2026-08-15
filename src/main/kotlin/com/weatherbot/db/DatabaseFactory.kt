package com.weatherbot.db

import com.weatherbot.config.BotConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.File

private val logger = LoggerFactory.getLogger("DatabaseFactory")

/**
 * SQLite 연결 초기화 및 테이블 생성을 담당한다.
 */
object DatabaseFactory {
    fun init() {
        val dbFile = File(BotConfig.dbPath)
        dbFile.parentFile?.mkdirs()

        Database.connect("jdbc:sqlite:${dbFile.path}", driver = "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(GuildConfigTable)
        }

        logger.info("DB 초기화 완료: ${dbFile.path}")
    }
}
