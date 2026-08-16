package com.weatherbot.weather.owm

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/** OpenWeatherMap 대기오염 API(`/data/2.5/air_pollution`) 호출을 담당한다. */
object OwmAirPollutionClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/air_pollution"
    private val httpClient = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    /** PM10 농도(µg/m³)를 조회한다. */
    fun fetchPm10(apiKey: String, lat: Double, lon: Double): Double {
        val url = "$BASE_URL?lat=$lat&lon=$lon&appid=$apiKey"

        val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            error("OpenWeatherMap 대기오염 API 호출 실패 (status=${response.statusCode()})")
        }

        val root = json.parseToJsonElement(response.body()).jsonObject
        val first = root["list"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: error("OpenWeatherMap 대기오염 API 응답 형식이 예상과 다릅니다.")

        return first["components"]?.jsonObject?.get("pm10")?.jsonPrimitive?.content?.toDoubleOrNull()
            ?: error("PM10 농도 정보를 찾을 수 없습니다.")
    }

    /** 한국 환경부 PM10 등급 기준(좋음 0~30, 보통 31~80, 나쁨 81~150, 매우나쁨 151~)으로 변환한다.
     * KmaWeatherProvider(에어코리아)와 동일한 라벨을 사용해 두 Provider 간 응답 포맷을 일관되게 유지한다. */
    fun pm10ToLabel(pm10: Double): String = when {
        pm10 <= 30 -> "좋음"
        pm10 <= 80 -> "보통"
        pm10 <= 150 -> "나쁨"
        else -> "매우나쁨"
    }
}
