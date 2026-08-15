package com.weatherbot.weather.kma

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class KmaForecastItem(
    val fcstDate: String,
    val fcstTime: String,
    val category: String,
    val fcstValue: String,
)

/** 기상청 단기예보 조회서비스(getVilageFcst) 호출을 담당한다. */
object KmaApiClient {
    private const val BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"
    private val httpClient = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun fetchForecast(serviceKey: String, baseDate: String, baseTime: String, nx: Int, ny: Int): List<KmaForecastItem> {
        // serviceKey는 공공데이터포털에서 발급 시 이미 URL 인코딩된 값이므로 재인코딩하지 않는다.
        val url = "$BASE_URL?serviceKey=$serviceKey&numOfRows=1000&pageNo=1&dataType=JSON" +
            "&base_date=$baseDate&base_time=$baseTime&nx=$nx&ny=$ny"

        val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            error("기상청 API 호출 실패 (status=${response.statusCode()})")
        }

        val root = json.parseToJsonElement(response.body()).jsonObject
        val header = root["response"]?.jsonObject?.get("header")?.jsonObject
        val resultCode = header?.get("resultCode")?.jsonPrimitive?.content
        if (resultCode != "00") {
            val resultMsg = header?.get("resultMsg")?.jsonPrimitive?.content ?: "알 수 없는 오류"
            error("기상청 API 오류 응답: $resultMsg")
        }

        val items = root["response"]?.jsonObject
            ?.get("body")?.jsonObject
            ?.get("items")?.jsonObject
            ?.get("item")?.jsonArray
            ?: error("기상청 API 응답 형식이 예상과 다릅니다.")

        return items.map { element ->
            val obj = element.jsonObject
            KmaForecastItem(
                fcstDate = obj["fcstDate"]?.jsonPrimitive?.content ?: error("fcstDate 필드가 없습니다."),
                fcstTime = obj["fcstTime"]?.jsonPrimitive?.content ?: error("fcstTime 필드가 없습니다."),
                category = obj["category"]?.jsonPrimitive?.content ?: error("category 필드가 없습니다."),
                fcstValue = obj["fcstValue"]?.jsonPrimitive?.content ?: error("fcstValue 필드가 없습니다."),
            )
        }
    }
}
