package com.weatherbot.weather.kma

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class KmaGridEntry(
    val name: String,
    val sido: String,
    val nx: Int,
    val ny: Int,
)

/**
 * 지역명 → 기상청 격자좌표(nx, ny)/시도명 매핑 테이블.
 * `src/main/resources/kma_grid.json`에서 로드하며, 지원 지역은 해당 파일에 항목을 추가해 확장한다.
 */
object KmaGridTable {
    private val entries: List<KmaGridEntry> by lazy {
        val stream = requireNotNull(javaClass.getResourceAsStream("/kma_grid.json")) {
            "kma_grid.json 리소스를 찾을 수 없습니다."
        }
        val text = stream.bufferedReader().use { it.readText() }
        Json.decodeFromString<List<KmaGridEntry>>(text)
    }

    fun find(location: String): KmaGridEntry {
        val trimmed = location.trim()
        return entries.firstOrNull { it.name == trimmed }
            ?: error("'$location'은(는) 지원하지 않는 지역입니다. 지원 지역: ${entries.joinToString { it.name }}")
    }
}
