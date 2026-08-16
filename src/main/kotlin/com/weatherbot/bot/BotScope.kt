package com.weatherbot.bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** 슬래시 커맨드 안에서 suspend 함수(날씨 API 호출 등)를 실행하기 위한 앱 전역 코루틴 스코프. */
object BotScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
