package com.weatherbot.weather.owm

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

data class GeoLocation(val lat: Double, val lon: Double)

/** OpenWeatherMap Geocoding API(`/geo/1.0/direct`) 호출을 담당한다. 지역명을 위도/경도로 변환한다. */
object OwmGeocodingClient {
    private const val BASE_URL = "https://api.openweathermap.org/geo/1.0/direct"
    private val httpClient = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun resolve(apiKey: String, location: String): GeoLocation {
        val encodedLocation = URLEncoder.encode("$location,KR", StandardCharsets.UTF_8)
        val url = "$BASE_URL?q=$encodedLocation&limit=1&appid=$apiKey"

        val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            error("OpenWeatherMap 지오코딩 API 호출 실패 (status=${response.statusCode()})")
        }

        val results = json.parseToJsonElement(response.body()).jsonArray
        val first = results.firstOrNull()?.jsonObject
            ?: error("'$location' 지역을 찾을 수 없습니다.")

        val lat = first["lat"]?.jsonPrimitive?.content?.toDoubleOrNull()
            ?: error("'$location' 지역의 위도 정보를 찾을 수 없습니다.")
        val lon = first["lon"]?.jsonPrimitive?.content?.toDoubleOrNull()
            ?: error("'$location' 지역의 경도 정보를 찾을 수 없습니다.")

        return GeoLocation(lat, lon)
    }
}
