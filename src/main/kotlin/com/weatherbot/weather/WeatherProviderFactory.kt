package com.weatherbot.weather

import com.weatherbot.weather.kma.KmaWeatherProvider
import com.weatherbot.weather.owm.OwmWeatherProvider

/** `guild_configs.weather_provider` 코드값으로 [WeatherProvider] 구현체를 생성한다. */
object WeatherProviderFactory {
    fun create(providerCode: String): WeatherProvider = when (providerCode) {
        "KMA" -> KmaWeatherProvider()
        "OWM" -> OwmWeatherProvider()
        else -> error("지원하지 않는 날씨 데이터 소스입니다: $providerCode")
    }
}
