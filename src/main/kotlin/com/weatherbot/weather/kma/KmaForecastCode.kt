package com.weatherbot.weather.kma

/** 기상청 SKY(하늘상태)/PTY(강수형태) 코드를 한글 날씨 텍스트로 변환한다. */
object KmaForecastCode {
    fun toConditionText(sky: String?, pty: String?): String {
        if (pty != null && pty != "0") {
            return when (pty) {
                "1" -> "비"
                "2" -> "비/눈"
                "3" -> "눈"
                "4" -> "소나기"
                "5" -> "빗방울"
                "6" -> "빗방울눈날림"
                "7" -> "눈날림"
                else -> "정보없음"
            }
        }
        return when (sky) {
            "1" -> "맑음"
            "3" -> "구름많음"
            "4" -> "흐림"
            else -> "정보없음"
        }
    }
}
