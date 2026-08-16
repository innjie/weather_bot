package com.weatherbot.weather

import java.time.format.DateTimeFormatter

/** [DailyWeather]를 Discord 메시지용 한국어 텍스트로 변환한다. 커맨드 응답/자동 알림에서 공통으로 사용한다. */
object WeatherMessageFormatter {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd")

    fun formatDaily(location: String, weather: DailyWeather): String {
        val dateStr = weather.date.format(DATE_FORMAT)
        return """
            [$location] ${dateStr} 날씨
            최저/최고기온: ${weather.minTemp}°C / ${weather.maxTemp}°C
            평균습도: ${"%.0f".format(weather.avgHumidity)}%
            미세먼지: ${weather.fineDustLevel}
            오전 날씨: ${weather.morningCondition}
            오후 날씨: ${weather.afternoonCondition}
        """.trimIndent()
    }
}
