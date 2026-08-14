package com.weatherbot.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * 길드(서버)별 날씨 알림 설정.
 * 길드당 1건만 존재한다 (guildId unique).
 */
object GuildConfigTable : Table("guild_configs") {
    val id = long("id").autoIncrement()
    val guildId = long("guild_id").uniqueIndex()
    val channelId = long("channel_id")
    val location = varchar("location", 100)
    val weatherProvider = varchar("weather_provider", 20)
    val notifyHour = integer("notify_hour")
    val notifyMinute = integer("notify_minute")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
