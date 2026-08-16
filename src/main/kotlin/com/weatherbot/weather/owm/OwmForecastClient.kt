package com.weatherbot.weather.owm

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

data class OwmForecastItem(
    val localDateTime: LocalDateTime,
    val temp: Double,
    val humidity: Double,
    val weatherMain: String,
)

/** OpenWeatherMap 5일/3시간 간격 예보 API(`/data/2.5/forecast`) 호출을 담당한다. */
object OwmForecastClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/forecast"
    private val REQUEST_TIMEOUT = Duration.ofSeconds(10)
    private val httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build()
    private val json = Json { ignoreUnknownKeys = true }

    fun fetchForecast(apiKey: String, lat: Double, lon: Double): List<OwmForecastItem> {
        val url = "$BASE_URL?lat=$lat&lon=$lon&units=metric&appid=$apiKey"

        val request = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            error("OpenWeatherMap 예보 API 호출 실패 (status=${response.statusCode()})")
        }

        val root = json.parseToJsonElement(response.body()).jsonObject
        val timezoneOffsetSeconds = root["city"]?.jsonObject?.get("timezone")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
        val list = root["list"]?.jsonArray ?: error("OpenWeatherMap 예보 API 응답 형식이 예상과 다릅니다.")

        return list.map { entry ->
            val obj = entry.jsonObject
            val dt = obj["dt"]?.jsonPrimitive?.content?.toLongOrNull()
                ?: error("예보 항목의 시각 정보를 찾을 수 없습니다.")
            val main = obj["main"]?.jsonObject ?: error("예보 항목의 기온/습도 정보를 찾을 수 없습니다.")
            val temp = main["temp"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: error("예보 항목의 기온 정보를 찾을 수 없습니다.")
            val humidity = main["humidity"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: error("예보 항목의 습도 정보를 찾을 수 없습니다.")
            val weatherMain = obj["weather"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("main")?.jsonPrimitive?.contentOrNull ?: "Unknown"

            val localDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(dt + timezoneOffsetSeconds),
                ZoneOffset.UTC,
            )

            OwmForecastItem(localDateTime, temp, humidity, weatherMain)
        }
    }
}
