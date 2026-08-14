package com.weatherbot.config

import io.github.cdimascio.dotenv.dotenv

/**
 * 환경변수(.env) 기반 설정.
 * 민감정보(토큰, API 키)를 코드에 하드코딩하지 않기 위해 사용한다.
 */
object BotConfig {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    private fun require(key: String): String =
        dotenv[key] ?: System.getenv(key)
        ?: error("환경변수 '$key'가 설정되지 않았습니다. .env 파일 또는 시스템 환경변수를 확인하세요.")

    val discordToken: String by lazy { require("DISCORD_TOKEN") }

    /** "KMA"(기상청+에어코리아) 또는 "OWM"(OpenWeatherMap) */
    val weatherProvider: String by lazy {
        dotenv["WEATHER_PROVIDER"] ?: System.getenv("WEATHER_PROVIDER") ?: "KMA"
    }
}
