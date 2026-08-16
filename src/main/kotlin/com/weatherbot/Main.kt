package com.weatherbot

import com.weatherbot.bot.CommandListener
import com.weatherbot.bot.commands.OtherCityWeatherCommand
import com.weatherbot.bot.commands.TodayWeatherCommand
import com.weatherbot.bot.commands.WeatherConfigCommand
import com.weatherbot.config.BotConfig
import com.weatherbot.db.DatabaseFactory
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Main")

private val commands = listOf(WeatherConfigCommand, TodayWeatherCommand, OtherCityWeatherCommand)

fun main() {
    DatabaseFactory.init()

    val jda = JDABuilder.createDefault(BotConfig.discordToken)
        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
        .addEventListeners(
            object : ListenerAdapter() {
                override fun onReady(event: ReadyEvent) {
                    logger.info("봇 로그인 완료: ${event.jda.selfUser.name}")
                }
            },
            CommandListener(commands),
        )
        .build()

    jda.awaitReady()

    // 글로벌 커맨드로 등록. 신규/변경 커맨드가 모든 서버에 반영되기까지 최대 1시간 소요될 수 있다.
    jda.updateCommands().addCommands(commands.map { it.data() }).queue()
}
