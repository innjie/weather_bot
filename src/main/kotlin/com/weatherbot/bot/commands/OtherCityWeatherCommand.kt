package com.weatherbot.bot.commands

import com.weatherbot.bot.BotScope
import com.weatherbot.weather.WeatherMessageFormatter
import com.weatherbot.weather.WeatherProviderFactory
import com.weatherbot.weather.kma.KmaGridTable
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.slf4j.LoggerFactory

/** `/도시날씨` 커맨드: 길드 설정과 무관하게 지정한 지역의 오늘 날씨를 조회한다. */
object OtherCityWeatherCommand : SlashCommand {
    private val logger = LoggerFactory.getLogger(OtherCityWeatherCommand::class.java)

    override val name = "도시날씨"

    override fun data(): CommandData = Commands.slash(name, "다른 지역의 오늘 날씨를 조회합니다.")
        .addOptions(
            OptionData(OptionType.STRING, "위치", "날씨를 조회할 지역명 (예: 부산)", true),
            OptionData(OptionType.STRING, "데이터소스", "날씨 데이터 소스 (기본: 기상청)", false)
                .addChoice("기상청", "KMA")
                .addChoice("OpenWeatherMap", "OWM"),
        )

    override fun execute(event: SlashCommandInteractionEvent) {
        val location = event.getOption("위치")?.asString?.trim()
        if (location.isNullOrBlank()) {
            event.reply("위치를 입력해주세요.").setEphemeral(true).queue()
            return
        }
        val providerCode = event.getOption("데이터소스")?.asString ?: "KMA"

        if (providerCode == "KMA") {
            try {
                KmaGridTable.find(location)
            } catch (e: IllegalStateException) {
                event.reply(e.message ?: "지원하지 않는 지역입니다.").setEphemeral(true).queue()
                return
            }
        }

        event.deferReply().queue()

        BotScope.scope.launch {
            try {
                val provider = WeatherProviderFactory.create(providerCode)
                val weather = provider.getToday(location)
                val message = WeatherMessageFormatter.formatDaily(location, weather)
                event.hook.sendMessage(message).queue()
            } catch (e: Exception) {
                logger.error("도시날씨 조회 실패: location=$location", e)
                // 내부 예외 메시지(외부 API 응답/URL 등)를 사용자에게 그대로 노출하지 않기 위해 일반화된 메시지만 응답한다.
                event.hook.sendMessage("날씨 조회 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.").queue()
            }
        }
    }
}
