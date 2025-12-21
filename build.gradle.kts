import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.serialization") version "1.9.25"
//    id("me.champeau.jmh") version "0.7.2"
    kotlin("plugin.allopen") version "1.9.25"
    kotlin("kapt") version "1.9.25" // Важно для MapStruct!
}

group = "tv.codealong.tutorials.springboot"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot and Kotlin and other interesting stuff"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

val javaVersion = JavaVersion.VERSION_21
val springmockkVersion = "3.1.2"
val mockkVersion = "1.13.4" //1.10.4
val kotlinVersion = "1.9.25"

java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion

extra["kotlin.version"] = kotlinVersion

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
//	maven { url = uri("https://repo.spring.io/milestone") }
//	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    //
    api("org.awaitility:awaitility:4.0.3")
    //корутины
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    //kafka
    implementation("org.springframework.kafka:spring-kafka")

    //ktor-client
    implementation("io.ktor:ktor-client-core:2.3.4")
    implementation("io.ktor:ktor-client-cio:2.3.4")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")


    //awaitility
    implementation("org.awaitility:awaitility:3.0.0")
    testImplementation("org.awaitility:awaitility:3.0.0")

    //jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") //LocalDatetime
    implementation("com.fasterxml.jackson.core:jackson-annotations")

    //Gson
    implementation("com.google.code.gson:gson:2.11.0")
    //feign
    implementation("io.github.openfeign:feign-core:13.5")

    // https://mvnrepository.com/artifact/io.github.oshai/kotlin-logging-jvm
//    implementation("io.github.microutils:koltin-logging-jvm:3.0.5") - не работает
    runtimeOnly("io.github.oshai:kotlin-logging-jvm:7.0.7")

    // Для распознавания текста из картинок
    implementation("net.sourceforge.tess4j:tess4j:5.8.0")

    //не знаю для чего, но в нем есть @NotNull, хотя такое есть у org.jetbrains.annotations.Nullable;
    //но в некоторых проектах была именно findbugs
    implementation("com.google.code.findbugs:jsr305:3.0.2")


    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    //JWT (Spring Security)
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("com.nimbusds:nimbus-jose-jwt")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Для генерации JWT токенов (если нужно создавать токены)
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")


    //MapStruct (решил обойтись без mapstruct, т.к. не взлетело и пока не стал разбираться)
//    implementation("org.mapstruct:mapstruct:1.5.5.Final")
//    implementation("org.mapstruct:mapstruct-processor:1.5.5.Final")
//    kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")

    // Для совместимости Lombok и MapStruct (если используете Lombok)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    // Но нужно настроить оба процессора для mapstruct
    //annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final") // Для Java классов
    //kapt("org.mapstruct:mapstruct-processor:1.5.5.Final") // Для Kotlin классов
    kapt("org.projectlombok:lombok:1.18.32")
    // Для совместимости Lombok + MapStruct
    //kapt("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    runtimeOnly("org.jetbrains.kotlin:kotlin-bom")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    //        <dependency>
    //            <groupId>com.h2database</groupId>
    //            <artifactId>h2</artifactId>
    //            <scope>runtime</scope>
    //        </dependency>
    annotationProcessor("org.projectlombok:lombok")

    //
//    jmh("org.openjdk.jmh:jmh-core:1.37")
//    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    //если без плагина
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")


    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    //wiremock
    //implementation("org.wiremock:wiremock-standalone:3.9.2")

    // Добавляем Jetty вручную
//    testImplementation("org.eclipse.jetty:jetty-server:9.4.51.v20230217")
//    testImplementation("org.eclipse.jetty:jetty-servlet:9.4.51.v20230217")
//    testImplementation("org.eclipse.jetty:jetty-util:9.4.51.v20230217")

    testImplementation("org.wiremock:wiremock-standalone:3.9.2")
//    testImplementation("javax.servlet:javax.servlet-api:4.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
//    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
    testImplementation("io.ktor:ktor-client-cio:2.3.4")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.4")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
//    testImplementation("org.springframework.boot:spring-security-test")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("com.ninja-squad:springmockk:${springmockkVersion}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.platform:junit-platform-engine")
//    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")


    //implementation(kotlin("stdlib-jdk8"))
//    если бы не использовал spring boot , то можно так подключить зависимости для тестов
//    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

//tasks.withType<KotlinCompile> {
//    kotlinOptions {
//        freeCompilerArgs = listOf("-Xjsr305=strict")
//        jvmTarget = "21"
//    }
//}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

kotlin {
    jvmToolchain(21)
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
//    annotation("org.openjdk.jmh.annotations.State")
}

// Конфигурация для kapt (Kotlin Annotation Processing)
kapt {
    keepJavacAnnotationProcessors = true
//    arguments { //это закомментировал, т.к. не стал использовать mapstruct
//        // Указываем componentModel для MapStruct
//        arg("mapstruct.defaultComponentModel", "spring")
//        arg("mapstruct.unmappedTargetPolicy", "IGNORE")
//    }
}


tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
    }
}

//эта таска нужна, если не использовать плагин jmh - id("me.champeau.jmh") version "0.7.2"
tasks.register<JavaExec>("jmh2") {
    group = "verification"
    description = "Run JMH benchmarks"
    classpath = sourceSets["test"].runtimeClasspath
//    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.openjdk.jmh.Main")
}

//Gradle (Kotlin DSL) с плагином jmh (Java Microbenchmark Harness)
//Самый простой вариант — использовать jmh‑плагин:
//plugins {
//    kotlin("jvm") version "1.9.25" // или твоя версия
//    id("org.springframework.boot") version "3.3.0" // пример
//    id("io.spring.dependency-management")
//    id("me.champeau.jmh") version "0.7.2"
//    kotlin("plugin.allopen") version "1.9.25"
//}
//
//repositories {
//    mavenCentral()
//}
//
//dependencies {
//    implementation("org.springframework.boot:spring-boot-starter")
//
//    jmh("org.openjdk.jmh:jmh-core:1.37")
//    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
//}
//
//allOpen {
//    annotation("org.openjdk.jmh.annotations.State")
//}