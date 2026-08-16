package com.weatherbot.bot.commands

import com.weatherbot.bot.BotScope
import com.weatherbot.db.GuildConfigRepository
import com.weatherbot.weather.WeatherMessageFormatter
import com.weatherbot.weather.WeatherProviderFactory
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory

/** `/주간날씨` 커맨드: 길드에 설정된 위치/데이터소스 기준으로 향후 며칠간의 날씨 요약을 응답한다. */
object WeeklyWeatherCommand : SlashCommand {
    private val logger = LoggerFactory.getLogger(WeeklyWeatherCommand::class.java)

    override val name = "주간날씨"

    override fun data(): CommandData = Commands.slash(name, "향후 며칠간의 날씨 요약을 조회합니다.")

    override fun execute(event: SlashCommandInteractionEvent) {
        val guild = event.guild
        if (guild == null) {
            event.reply("이 커맨드는 서버(길드) 안에서만 사용할 수 있습니다.").setEphemeral(true).queue()
            return
        }

        val config = GuildConfigRepository.find(guild.idLong)
        if (config == null) {
            event.reply("먼저 `/날씨설정` 커맨드로 위치와 알림 설정을 등록해주세요.").setEphemeral(true).queue()
            return
        }

        event.deferReply().queue()

        BotScope.scope.launch {
            try {
                val provider = WeatherProviderFactory.create(config.weatherProvider)
                val weekly = provider.getWeeklyForecast(config.location)
                val message = WeatherMessageFormatter.formatWeekly(config.location, weekly)
                event.hook.sendMessage(message).queue()
            } catch (e: Exception) {
                logger.error("주간날씨 조회 실패: guildId=${guild.idLong}", e)
                // 내부 예외 메시지(외부 API 응답/URL 등)를 사용자에게 그대로 노출하지 않기 위해 일반화된 메시지만 응답한다.
                event.hook.sendMessage("날씨 조회 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.").queue()
            }
        }
    }
}
