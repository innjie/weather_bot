plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.weatherbot"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Discord
    implementation("net.dv8tion:JDA:5.6.1")

    // 코루틴 기반 스케줄러
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // 저장소: SQLite + Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.61.0")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")

    // 외부 API 응답 파싱
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // 환경변수(.env) 로딩 - 민감정보 하드코딩 방지
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    // 로깅
    implementation("ch.qos.logback:logback-classic:1.5.18")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.weatherbot.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("weather-bot")
    archiveClassifier.set("")
}
