package com.weatherbot.bot.commands

import com.weatherbot.db.GuildConfigRepository
import com.weatherbot.weather.kma.KmaGridTable
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData

/**
 * `/날씨설정` 커맨드: 길드의 날씨 알림 위치/시각/데이터소스를 설정한다.
 * 알림 채널은 커맨드를 실행한 채널로 자동 지정된다.
 */
object WeatherConfigCommand : SlashCommand {
    override val name = "날씨설정"

    override fun data(): CommandData = Commands.slash(name, "날씨 알림 위치/시각/데이터소스를 설정합니다.")
        .addOptions(
            OptionData(OptionType.STRING, "위치", "날씨를 조회할 지역명 (예: 서울)", true),
            OptionData(OptionType.INTEGER, "시", "알림 발송 시(0~23)", true)
                .setRequiredRange(0, 23),
            OptionData(OptionType.INTEGER, "분", "알림 발송 분(0~59, 기본 0)", false)
                .setRequiredRange(0, 59),
            OptionData(OptionType.STRING, "데이터소스", "날씨 데이터 소스 (기본: 기상청)", false)
                .addChoice("기상청", "KMA")
                .addChoice("OpenWeatherMap", "OWM"),
        )

    override fun execute(event: SlashCommandInteractionEvent) {
        val guild = event.guild
        if (guild == null) {
            event.reply("이 커맨드는 서버(길드) 안에서만 사용할 수 있습니다.").setEphemeral(true).queue()
            return
        }

        val location = event.getOption("위치")?.asString?.trim()
        if (location.isNullOrBlank()) {
            event.reply("위치를 입력해주세요.").setEphemeral(true).queue()
            return
        }

        val notifyHour = event.getOption("시")?.asInt
        if (notifyHour == null) {
            event.reply("알림 시각(시)을 입력해주세요.").setEphemeral(true).queue()
            return
        }
        val notifyMinute = event.getOption("분")?.asInt ?: 0
        val weatherProvider = event.getOption("데이터소스")?.asString ?: "KMA"

        if (weatherProvider == "KMA") {
            try {
                KmaGridTable.find(location)
            } catch (e: IllegalStateException) {
                event.reply(e.message ?: "지원하지 않는 지역입니다.").setEphemeral(true).queue()
                return
            }
        }

        val config = GuildConfigRepository.upsert(
            guildId = guild.idLong,
            channelId = event.channel.idLong,
            location = location,
            weatherProvider = weatherProvider,
            notifyHour = notifyHour,
            notifyMinute = notifyMinute,
        )

        val providerLabel = if (config.weatherProvider == "KMA") "기상청" else "OpenWeatherMap"
        event.reply(
            "날씨 알림 설정이 저장되었습니다.\n" +
                "- 위치: ${config.location}\n" +
                "- 알림 채널: <#${config.channelId}>\n" +
                "- 알림 시각: %02d:%02d".format(config.notifyHour, config.notifyMinute) + "\n" +
                "- 데이터소스: $providerLabel",
        ).queue()
    }
}
