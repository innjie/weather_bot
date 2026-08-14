package com.weatherbot

import com.weatherbot.config.BotConfig
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Main")

fun main() {
    val jda = JDABuilder.createDefault(BotConfig.discordToken)
        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
        .addEventListeners(object : ListenerAdapter() {
            override fun onReady(event: ReadyEvent) {
                logger.info("봇 로그인 완료: ${event.jda.selfUser.name}")
            }
        })
        .build()

    jda.awaitReady()
}
