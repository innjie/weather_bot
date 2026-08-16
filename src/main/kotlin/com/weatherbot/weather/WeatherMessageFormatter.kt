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

    /**
     * 여러 날짜의 [DailyWeather]를 하나의 메시지로 묶어 포맷한다.
     * 실제 제공 일수는 데이터 소스(KMA/OWM)마다 다르므로, 헤더에 실제 반환된 일수를 명시한다.
     */
    fun formatWeekly(location: String, days: List<DailyWeather>): String {
        if (days.isEmpty()) {
            return "[$location] 조회 가능한 예보 데이터가 없습니다."
        }

        val header = "[$location] 향후 ${days.size}일간 날씨"
        val body = days.joinToString("\n\n") { weather ->
            val dateStr = weather.date.format(DATE_FORMAT)
            """
                $dateStr
                최저/최고기온: ${weather.minTemp}°C / ${weather.maxTemp}°C
                평균습도: ${"%.0f".format(weather.avgHumidity)}%
                미세먼지: ${weather.fineDustLevel}
                오전 날씨: ${weather.morningCondition}
                오후 날씨: ${weather.afternoonCondition}
            """.trimIndent()
        }
        return "$header\n\n$body"
    }
}
