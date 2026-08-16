package com.weatherbot.scheduler

import com.weatherbot.bot.BotScope
import com.weatherbot.db.GuildConfig
import com.weatherbot.db.GuildConfigRepository
import com.weatherbot.weather.WeatherMessageFormatter
import com.weatherbot.weather.WeatherProviderFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 매 분 정각마다 길드별 알림 설정(`guild_configs`)을 확인해, 설정된 시각과 일치하면
 * 해당 채널로 오늘의 날씨 요약을 발송한다.
 */
class NotificationScheduler(private val jda: JDA) {
    private val logger = LoggerFactory.getLogger(NotificationScheduler::class.java)

    fun start() {
        BotScope.scope.launch {
            while (isActive) {
                sleepUntilNextMinute()
                val now = LocalTime.now()
                runCatching { notifyMatchingGuilds(now.hour, now.minute) }
                    .onFailure { logger.error("알림 대상 조회 중 오류 발생", it) }
            }
        }
        logger.info("알림 스케줄러 시작")
    }

    private suspend fun sleepUntilNextMinute() {
        val now = LocalDateTime.now()
        val next = now.plusMinutes(1).withSecond(0).withNano(0)
        delay(Duration.between(now, next).toMillis())
    }

    private fun notifyMatchingGuilds(hour: Int, minute: Int) {
        val targets = GuildConfigRepository.findAll()
            .filter { it.notifyHour == hour && it.notifyMinute == minute }

        targets.forEach { config ->
            BotScope.scope.launch {
                sendNotification(config)
            }
        }
    }

    private suspend fun sendNotification(config: GuildConfig) {
        try {
            val channel = jda.getChannelById(MessageChannel::class.java, config.channelId)
            if (channel == null) {
                logger.warn("알림 채널을 찾을 수 없습니다: guildId=${config.guildId}, channelId=${config.channelId}")
                return
            }

            val provider = WeatherProviderFactory.create(config.weatherProvider)
            val weather = provider.getToday(config.location)
            val message = WeatherMessageFormatter.formatDaily(config.location, weather)

            channel.sendMessage(message).queue()
        } catch (e: Exception) {
            logger.error("알림 발송 실패: guildId=${config.guildId}, location=${config.location}", e)
        }
    }
}
