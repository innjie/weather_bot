package com.weatherbot.db

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

data class GuildConfig(
    val guildId: Long,
    val channelId: Long,
    val location: String,
    val weatherProvider: String,
    val notifyHour: Int,
    val notifyMinute: Int,
)

/** `guild_configs` 테이블 접근을 담당한다. 길드당 설정 1건(upsert)만 허용한다. */
object GuildConfigRepository {

    fun find(guildId: Long): GuildConfig? = transaction {
        GuildConfigTable.selectAll()
            .where { GuildConfigTable.guildId eq guildId }
            .map { it.toGuildConfig() }
            .firstOrNull()
    }

    fun findAll(): List<GuildConfig> = transaction {
        GuildConfigTable.selectAll().map { it.toGuildConfig() }
    }

    fun upsert(
        guildId: Long,
        channelId: Long,
        location: String,
        weatherProvider: String,
        notifyHour: Int,
        notifyMinute: Int,
    ): GuildConfig = transaction {
        val now = Instant.now()
        val exists = GuildConfigTable.selectAll()
            .where { GuildConfigTable.guildId eq guildId }
            .any()

        if (exists) {
            GuildConfigTable.update({ GuildConfigTable.guildId eq guildId }) {
                it[GuildConfigTable.channelId] = channelId
                it[GuildConfigTable.location] = location
                it[GuildConfigTable.weatherProvider] = weatherProvider
                it[GuildConfigTable.notifyHour] = notifyHour
                it[GuildConfigTable.notifyMinute] = notifyMinute
                it[updatedAt] = now
            }
        } else {
            GuildConfigTable.insert {
                it[GuildConfigTable.guildId] = guildId
                it[GuildConfigTable.channelId] = channelId
                it[GuildConfigTable.location] = location
                it[GuildConfigTable.weatherProvider] = weatherProvider
                it[GuildConfigTable.notifyHour] = notifyHour
                it[GuildConfigTable.notifyMinute] = notifyMinute
                it[createdAt] = now
                it[updatedAt] = now
            }
        }

        GuildConfig(guildId, channelId, location, weatherProvider, notifyHour, notifyMinute)
    }

    private fun org.jetbrains.exposed.sql.ResultRow.toGuildConfig() = GuildConfig(
        guildId = this[GuildConfigTable.guildId],
        channelId = this[GuildConfigTable.channelId],
        location = this[GuildConfigTable.location],
        weatherProvider = this[GuildConfigTable.weatherProvider],
        notifyHour = this[GuildConfigTable.notifyHour],
        notifyMinute = this[GuildConfigTable.notifyMinute],
    )
}
