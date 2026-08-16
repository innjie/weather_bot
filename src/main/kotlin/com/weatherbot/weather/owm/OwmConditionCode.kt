package com.weatherbot.weather.owm

object OwmConditionCode {
    fun toConditionText(weatherMain: String): String = when (weatherMain) {
        "Clear" -> "맑음"
        "Clouds" -> "구름많음"
        "Rain", "Drizzle" -> "비"
        "Thunderstorm" -> "천둥번개"
        "Snow" -> "눈"
        "Mist", "Fog", "Haze" -> "흐림"
        else -> "정보없음"
    }
}
