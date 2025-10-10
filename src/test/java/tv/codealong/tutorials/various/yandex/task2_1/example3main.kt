package tv.codealong.tutorials.various.yandex.task2_1

/**
 * 🔄 СРАВНЕНИЕ АЛГОРИТМОВ
 * Давай также добавим тест, который покажет разницу между алгоритмами:
 *  СРАВНЕНИЕ TOKEN BUCKET vs FIXED WINDOW
 */
fun main() {
    compareAlgorithms()
}

fun compareAlgorithms() {
    println("=== 🔄 СРАВНЕНИЕ TOKEN BUCKET vs FIXED WINDOW ===")

    val rateLimiter = DistributedRateLimiter()

    // Настраиваем одинаковые лимиты разными алгоритмами
    rateLimiter.configureEndpoint(
        "/api/token",
        RateLimitConfig(
            maxRequests = 5,
            windowSize = java.time.Duration.ofSeconds(3),
            algorithm = RateLimitAlgorithm.TOKEN_BUCKET
        )
    )

    rateLimiter.configureEndpoint(
        "/api/fixed",
        RateLimitConfig(
            maxRequests = 5,
            windowSize = java.time.Duration.ofSeconds(3),
            algorithm = RateLimitAlgorithm.FIXED_WINDOW
        )
    )

    val userId = "comparisonUser"

    println("🔹 6 быстрых запросов к разным endpoint'ам:")
    repeat(6) { i ->
        val tokenResult = rateLimiter.tryAcquire(userId, "/api/token")
        val fixedResult = rateLimiter.tryAcquire(userId, "/api/fixed")

        println("Запрос ${i + 1}:")
        println("  Token Bucket: ${if (tokenResult.allowed) "✅" else "❌"} (${tokenResult.remaining} осталось)")
        println("  Fixed Window: ${if (fixedResult.allowed) "✅" else "❌"} (${fixedResult.remaining} осталось)")
        println()

        Thread.sleep(100)
    }

    // Ждём половину окна и тестируем снова
    println("⏳ Ждём 1.5 секунды...")
    Thread.sleep(1500)

    println("🔹 Запросы после частичного ожидания:")
    val tokenResult = rateLimiter.tryAcquire(userId, "/api/token")
    val fixedResult = rateLimiter.tryAcquire(userId, "/api/fixed")

    println("Token Bucket: ${if (tokenResult.allowed) "✅" else "❌"} (${tokenResult.remaining} осталось)")
    println("Fixed Window: ${if (fixedResult.allowed) "✅" else "❌"} (${fixedResult.remaining} осталось)")
}