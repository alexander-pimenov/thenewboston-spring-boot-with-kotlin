package tv.codealong.tutorials.various.yandex.task2_1


/**
 * 🚀 ФИНАЛЬНЫЙ ТЕСТ ВСЕЙ СИСТЕМЫ
 */
fun main() {
    testSlidingWindow()
    println("\n" + "==================================================" + "\n")
    compareAllAlgorithms()
    println("\n" + "==================================================" + "\n")
    performanceTest()

    // Демонстрация использования в реальном API
    demonstrateRealWorldUsage()
}

fun demonstrateRealWorldUsage() {
    println("=== 🌍 РЕАЛЬНОЕ ИСПОЛЬЗОВАНИЕ ===")

    val rateLimiter = DistributedRateLimiter()

    // Разные лимиты для разных endpoint'ов
    rateLimiter.configureEndpoint(
        "/api/auth/login",
        RateLimitConfig(5, java.time.Duration.ofMinutes(1), RateLimitAlgorithm.SLIDING_WINDOW)
    )

    rateLimiter.configureEndpoint(
        "/api/search",
        RateLimitConfig(100, java.time.Duration.ofSeconds(10), RateLimitAlgorithm.TOKEN_BUCKET)
    )

    rateLimiter.configureEndpoint(
        "/api/upload",
        RateLimitConfig(20, java.time.Duration.ofMinutes(1), RateLimitAlgorithm.FIXED_WINDOW)
    )

    println("✅ Система готова к работе!")
    println("Лимиты настроены для:")
    println("  - /api/auth/login: 5 запросов в минуту (Sliding Window)")
    println("  - /api/search: 100 запросов в 10 секунд (Token Bucket)")
    println("  - /api/upload: 20 запросов в минуту (Fixed Window)")
}