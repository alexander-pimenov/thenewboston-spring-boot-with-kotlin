import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.7.10"
    id("org.springframework.boot") version "2.6.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

group = "tv.codealong.tutorials.springboot"
version = "0.0.1-SNAPSHOT"

val javaVersion = JavaVersion.VERSION_17
val springmockkVersion = "3.1.2"
val mockkVersion = "1.13.4" //1.10.4
val kotlinVersion = "1.7.10"

java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion

extra["kotlin.version"] = kotlinVersion

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



    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    runtimeOnly("org.jetbrains.kotlin:kotlin-bom")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("com.ninja-squad:springmockk:${springmockkVersion}")

    //implementation(kotlin("stdlib-jdk8"))
//    если бы не использовал spring boot , то можно так подключить зависимости для тестов
//    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
