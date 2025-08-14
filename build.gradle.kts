import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.serialization") version "1.9.25"
}

group = "tv.codealong.tutorials.springboot"
version = "0.0.1-SNAPSHOT"

//java {
//    toolchain {
//        languageVersion = JavaLanguageVersion.of(21)
//    }
//}

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

    //awaitility
    implementation("org.awaitility:awaitility:3.0.0")
    testImplementation("org.awaitility:awaitility:3.0.0")

    //jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") //LocalDatetime
    implementation("com.fasterxml.jackson.core:jackson-annotations")

    //feign
    implementation("io.github.openfeign:feign-core:13.5")

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
//    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    runtimeOnly("org.jetbrains.kotlin:kotlin-bom")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("com.ninja-squad:springmockk:${springmockkVersion}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
//    testImplementation("org.springframework.security:spring-security-test")



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
}

tasks.withType<Test> {
    useJUnitPlatform()
}
