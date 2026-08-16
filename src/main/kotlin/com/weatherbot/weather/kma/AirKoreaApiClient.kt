package com.weatherbot.weather.kma

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/** 에어코리아 시도별 실시간 측정정보 조회서비스(getCtprvnRltmMesureDnsty) 호출을 담당한다. */
object AirKoreaApiClient {
    private const val BASE_URL = "https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty"
    private val REQUEST_TIMEOUT = Duration.ofSeconds(10)
    private val httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build()
    private val json = Json { ignoreUnknownKeys = true }

    /** 시도 내 측정소들 중 유효한 PM10 등급(1~4)을 조회한다. */
    fun fetchPm10Grade(serviceKey: String, sidoName: String): Int {
        val encodedSido = URLEncoder.encode(sidoName, StandardCharsets.UTF_8)
        // serviceKey는 공공데이터포털에서 발급 시 이미 URL 인코딩된 값이므로 재인코딩하지 않는다.
        val url = "$BASE_URL?serviceKey=$serviceKey&returnType=json&numOfRows=100&pageNo=1" +
            "&sidoName=$encodedSido&ver=1.0"

        val request = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            error("에어코리아 API 호출 실패 (status=${response.statusCode()})")
        }

        val root = json.parseToJsonElement(response.body()).jsonObject
        val header = root["response"]?.jsonObject?.get("header")?.jsonObject
        val resultCode = header?.get("resultCode")?.jsonPrimitive?.content
        if (resultCode != "00") {
            val resultMsg = header?.get("resultMsg")?.jsonPrimitive?.content ?: "알 수 없는 오류"
            error("에어코리아 API 오류 응답: $resultMsg")
        }

        val items = root["response"]?.jsonObject
            ?.get("body")?.jsonObject
            ?.get("items")?.jsonArray
            ?: error("에어코리아 API 응답 형식이 예상과 다릅니다.")

        val grade = items.asSequence()
            .map { it.jsonObject }
            .mapNotNull { obj -> obj["pm10Grade"]?.jsonPrimitive?.contentOrNull }
            .firstOrNull { it.isNotBlank() }
            ?: error("'$sidoName' 지역의 미세먼지 측정 데이터를 찾을 수 없습니다.")

        return grade.toIntOrNull() ?: error("미세먼지 등급 값을 해석할 수 없습니다: $grade")
    }

    fun gradeToLabel(grade: Int): String = when (grade) {
        1 -> "좋음"
        2 -> "보통"
        3 -> "나쁨"
        4 -> "매우나쁨"
        else -> "정보없음"
    }
}
