package tv.codealong.tutorials.various.yandex.task2_1

fun main() {
    val rateLimiter = DistributedRateLimiter()

    // Настраиваем лимиты для разных endpoint'ов (1)
    rateLimiter.configureEndpoint(
        "/api/users",
        RateLimitConfig(
            maxRequests = 100,
            windowSize = java.time.Duration.ofMinutes(1),
            algorithm = RateLimitAlgorithm.TOKEN_BUCKET
        )
    )

    // Настраиваем лимиты для разных endpoint'ов (2)
    rateLimiter.configureEndpoint(
        "/api/payments",
        RateLimitConfig(
            maxRequests = 10,
            windowSize = java.time.Duration.ofSeconds(30),
            algorithm = RateLimitAlgorithm.SLIDING_WINDOW
        )
    )

    // Тестируем
    repeat(15) { i ->
        val result = rateLimiter.tryAcquire("user123", "/api/payments")
        println("Запрос $i: ${if (result.allowed) "✅" else "❌"} - осталось ${result.remaining}")
    }
}