package tv.codealong.tutorials.various.yandex.task2_1


fun main() {
    performanceTest()
}


/**
 * 📊 АНАЛИЗ ПРОИЗВОДИТЕЛЬНОСТИ
 */
fun performanceTest() {
    println("=== 📊 ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ===")

    val rateLimiter = DistributedRateLimiter()
    val testDuration = java.time.Duration.ofSeconds(3)

    val algorithms = listOf(
        "Token Bucket" to RateLimitAlgorithm.TOKEN_BUCKET,
        "Fixed Window" to RateLimitAlgorithm.FIXED_WINDOW,
        "Sliding Window" to RateLimitAlgorithm.SLIDING_WINDOW
    )

    algorithms.forEach { (name, algorithm) ->
        val endpoint = "/api/perf/$name"
        rateLimiter.configureEndpoint(
            endpoint,
            RateLimitConfig(1000, testDuration, algorithm)
        )

        val startTime = System.currentTimeMillis()
        var requestCount = 0
        var allowedCount = 0

        // Выполняем запросы в течение 1 секунды
        while (System.currentTimeMillis() - startTime < 1000) {
            val result = rateLimiter.tryAcquire("perfUser", endpoint)
            requestCount++
            if (result.allowed) allowedCount++
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        println("$name:")
        println("  Запросов/секунду: ${requestCount * 1000 / duration}")
        println("  Успешных запросов: $allowedCount")
        println("  Время на запрос: ${duration.toDouble() / requestCount} мс")
        println()
    }
}