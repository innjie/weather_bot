package com.weatherbot.weather.owm

import com.weatherbot.config.BotConfig
import com.weatherbot.weather.DailyWeather
import com.weatherbot.weather.WeatherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * OpenWeatherMap 5일/3시간 간격 예보 + 대기오염 API를 조합한 [WeatherProvider] 구현체.
 * 지역명은 지오코딩(OwmGeocodingClient)으로 위도/경도로 변환하므로, 별도 좌표표 관리 없이
 * OWM이 지원하는 지명이면 코드 변경 없이 바로 조회 가능하다.
 */
class OwmWeatherProvider : WeatherProvider {

    override suspend fun getToday(location: String): DailyWeather = withContext(Dispatchers.IO) {
        val geo = OwmGeocodingClient.resolve(BotConfig.owmApiKey, location)
        val items = OwmForecastClient.fetchForecast(BotConfig.owmApiKey, geo.lat, geo.lon)
        val pm10 = OwmAirPollutionClient.fetchPm10(BotConfig.owmApiKey, geo.lat, geo.lon)

        buildDailyWeather(items, LocalDate.now(), pm10)
    }

    override suspend fun getWeeklyForecast(location: String): List<DailyWeather> = withContext(Dispatchers.IO) {
        val geo = OwmGeocodingClient.resolve(BotConfig.owmApiKey, location)
        val items = OwmForecastClient.fetchForecast(BotConfig.owmApiKey, geo.lat, geo.lon)
        val pm10 = OwmAirPollutionClient.fetchPm10(BotConfig.owmApiKey, geo.lat, geo.lon)

        items.map { it.localDateTime.toLocalDate() }.distinct().sorted().map { date ->
            buildDailyWeather(items, date, pm10)
        }
    }

    private fun buildDailyWeather(items: List<OwmForecastItem>, date: LocalDate, pm10: Double): DailyWeather {
        val dayItems = items.filter { it.localDateTime.toLocalDate() == date }
        if (dayItems.isEmpty()) {
            error("$date 에 대한 예보 데이터를 찾을 수 없습니다.")
        }

        val minTemp = dayItems.minOf { it.temp }
        val maxTemp = dayItems.maxOf { it.temp }
        val avgHumidity = dayItems.map { it.humidity }.average()

        return DailyWeather(
            date = date,
            minTemp = minTemp,
            maxTemp = maxTemp,
            avgHumidity = avgHumidity,
            fineDustLevel = OwmAirPollutionClient.pm10ToLabel(pm10),
            morningCondition = conditionAt(dayItems, MORNING_HOURS),
            afternoonCondition = conditionAt(dayItems, AFTERNOON_HOURS),
        )
    }

    private fun conditionAt(dayItems: List<OwmForecastItem>, candidateHours: List<Int>): String {
        val item = candidateHours.firstNotNullOfOrNull { hour ->
            dayItems.firstOrNull { it.localDateTime.hour == hour }
        } ?: return "정보없음"
        return OwmConditionCode.toConditionText(item.weatherMain)
    }

    companion object {
        private val MORNING_HOURS = listOf(9, 6, 12)
        private val AFTERNOON_HOURS = listOf(15, 18, 12)
    }
}
