package com.weatherbot.weather

import java.time.LocalDate

/**
 * 특정 날짜의 날씨/미세먼지 요약 정보.
 */
data class DailyWeather(
    val date: LocalDate,
    val minTemp: Double,
    val maxTemp: Double,
    val avgHumidity: Double,
    val fineDustLevel: String,
    val morningCondition: String,
    val afternoonCondition: String,
)

/**
 * 날씨/미세먼지 데이터 소스 추상화.
 * 기상청+에어코리아, OpenWeatherMap 등 구체 구현체가 이 인터페이스를 따른다.
 */
interface WeatherProvider {
    suspend fun getToday(location: String): DailyWeather
    suspend fun getWeeklyForecast(location: String): List<DailyWeather>
}
