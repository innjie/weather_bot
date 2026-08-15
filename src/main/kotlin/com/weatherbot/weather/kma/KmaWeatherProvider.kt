package com.weatherbot.weather.kma

import com.weatherbot.config.BotConfig
import com.weatherbot.weather.DailyWeather
import com.weatherbot.weather.WeatherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 기상청 단기예보 + 에어코리아 미세먼지를 조합한 [WeatherProvider] 구현체.
 * 단기예보는 최대 3일치까지만 상세 데이터를 제공하므로, [getWeeklyForecast]는
 * 조회 시점 기준 조회 가능한 일자만 반환한다(7일 전체 지원은 중기예보 API 연동이 필요, 추후 TODO).
 */
class KmaWeatherProvider : WeatherProvider {

    override suspend fun getToday(location: String): DailyWeather = withContext(Dispatchers.IO) {
        val grid = KmaGridTable.find(location)
        val (baseDate, baseTime) = latestBaseDateTime()
        val items = KmaApiClient.fetchForecast(BotConfig.kmaApiKey, baseDate, baseTime, grid.nx, grid.ny)
        val pm10Grade = AirKoreaApiClient.fetchPm10Grade(BotConfig.airKoreaApiKey, grid.sido)

        buildDailyWeather(items, LocalDate.now(), pm10Grade)
    }

    override suspend fun getWeeklyForecast(location: String): List<DailyWeather> = withContext(Dispatchers.IO) {
        val grid = KmaGridTable.find(location)
        val (baseDate, baseTime) = latestBaseDateTime()
        val items = KmaApiClient.fetchForecast(BotConfig.kmaApiKey, baseDate, baseTime, grid.nx, grid.ny)
        val pm10Grade = AirKoreaApiClient.fetchPm10Grade(BotConfig.airKoreaApiKey, grid.sido)

        items.map { it.fcstDate }.distinct().sorted().map { dateStr ->
            buildDailyWeather(items, LocalDate.parse(dateStr, DATE_FORMAT), pm10Grade)
        }
    }

    private fun buildDailyWeather(items: List<KmaForecastItem>, date: LocalDate, pm10Grade: Int): DailyWeather {
        val dateStr = date.format(DATE_FORMAT)
        val dayItems = items.filter { it.fcstDate == dateStr }
        if (dayItems.isEmpty()) {
            error("$date 에 대한 예보 데이터를 찾을 수 없습니다.")
        }

        val tmpValues = dayItems.filter { it.category == "TMP" }.mapNotNull { it.fcstValue.toDoubleOrNull() }
        val minTemp = dayItems.firstOrNull { it.category == "TMN" }?.fcstValue?.toDoubleOrNull()
            ?: tmpValues.minOrNull()
            ?: error("$date 최저기온 데이터를 찾을 수 없습니다.")
        val maxTemp = dayItems.firstOrNull { it.category == "TMX" }?.fcstValue?.toDoubleOrNull()
            ?: tmpValues.maxOrNull()
            ?: error("$date 최고기온 데이터를 찾을 수 없습니다.")

        val humidityValues = dayItems.filter { it.category == "REH" }.mapNotNull { it.fcstValue.toDoubleOrNull() }
        val avgHumidity = if (humidityValues.isNotEmpty()) humidityValues.average() else 0.0

        return DailyWeather(
            date = date,
            minTemp = minTemp,
            maxTemp = maxTemp,
            avgHumidity = avgHumidity,
            fineDustLevel = AirKoreaApiClient.gradeToLabel(pm10Grade),
            morningCondition = conditionAt(dayItems, MORNING_TIMES),
            afternoonCondition = conditionAt(dayItems, AFTERNOON_TIMES),
        )
    }

    private fun conditionAt(dayItems: List<KmaForecastItem>, candidateTimes: List<String>): String {
        val time = candidateTimes.firstOrNull { t -> dayItems.any { it.fcstTime == t } }
            ?: return "정보없음"
        val sky = dayItems.firstOrNull { it.category == "SKY" && it.fcstTime == time }?.fcstValue
        val pty = dayItems.firstOrNull { it.category == "PTY" && it.fcstTime == time }?.fcstValue
        return KmaForecastCode.toConditionText(sky, pty)
    }

    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd")
        private val MORNING_TIMES = listOf("0900", "0600", "1200")
        private val AFTERNOON_TIMES = listOf("1500", "1800", "1200")

        /** 기상청 단기예보 발표시각(02,05,08,11,14,17,20,23시, +10분 지연) 중 현재 조회 가능한 가장 최근 값을 계산한다. */
        private fun latestBaseDateTime(now: LocalDateTime = LocalDateTime.now()): Pair<String, String> {
            val publishHours = listOf(23, 20, 17, 14, 11, 8, 5, 2)
            val adjusted = now.minusMinutes(10)
            val hour = publishHours.firstOrNull { it <= adjusted.hour }
            return if (hour != null) {
                adjusted.toLocalDate().format(DATE_FORMAT) to "%02d00".format(hour)
            } else {
                adjusted.toLocalDate().minusDays(1).format(DATE_FORMAT) to "2300"
            }
        }
    }
}
