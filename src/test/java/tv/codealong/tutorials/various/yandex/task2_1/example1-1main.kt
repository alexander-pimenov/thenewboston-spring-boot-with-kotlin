package tv.codealong.tutorials.various.yandex.task2_1

fun main() {
    quickTokenBucketTest()
}

fun quickTokenBucketTest() {
    println("=== 🔧 ТЕСТИРУЕМ ИСПРАВЛЕННЫЙ TOKEN BUCKET ===")

    val rateLimiter = DistributedRateLimiter()

    rateLimiter.configureEndpoint(
        "/api/test",
        RateLimitConfig(
            maxRequests = 5,
            windowSize = java.time.Duration.ofSeconds(3),
            algorithm = RateLimitAlgorithm.TOKEN_BUCKET
        )
    )

    val userId = "testUser"
    val endpoint = "/api/test"

    println("🔹 Делаем 7 запросов (лимит: 5/3сек):")
    repeat(7) { i ->
        val result = rateLimiter.tryAcquire(userId, endpoint)
        println("Запрос ${i + 1}: ${if (result.allowed) "✅" else "❌"} - ${result.remaining} токенов осталось")
        Thread.sleep(100)
    }

    println("\n⏳ Ждём 2 секунды для пополнения...")
    Thread.sleep(2000)

    println("🔹 Ещё 3 запроса после ожидания:")
    repeat(3) { i ->
        val result = rateLimiter.tryAcquire(userId, endpoint)
        println("Запрос ${i + 1}: ${if (result.allowed) "✅" else "❌"} - ${result.remaining} токенов осталось")
    }
}