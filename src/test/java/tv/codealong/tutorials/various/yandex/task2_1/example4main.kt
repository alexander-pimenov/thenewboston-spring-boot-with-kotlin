package tv.codealong.tutorials.various.yandex.task2_1

import java.time.Instant


fun main(){
    testSlidingWindow()
}

/**
 * 🧪 ТЕСТИРУЕМ SLIDING WINDOW
 */
fun testSlidingWindow() {
    println("=== 🚪 ТЕСТИРОВАНИЕ SLIDING WINDOW ===")

    val rateLimiter = DistributedRateLimiter()

    // Настраиваем Sliding Window: 10 запросов в 5 секунд
    rateLimiter.configureEndpoint(
        "/api/analytics",
        RateLimitConfig(
            maxRequests = 10,
            windowSize = java.time.Duration.ofSeconds(5),
            algorithm = RateLimitAlgorithm.SLIDING_WINDOW
        )
    )

    val userId = "user789"
    val endpoint = "/api/analytics"

    // Тест 1: Равномерные запросы
    println("🔹 ТЕСТ 1: 12 запросов с интервалом 0.5с (лимит: 10/5сек)")
    repeat(12) { i ->
        val result = rateLimiter.tryAcquire(userId, endpoint)
        println("Запрос ${i + 1}: ${if (result.allowed) "✅" else "❌"} " +
                "- ${result.remaining} осталось, " +
                "сброс через ${java.time.Duration.between(Instant.now(), result.resetTime!!).seconds}с")
        Thread.sleep(500)
    }

    // Тест 2: Проверка "скользящего" эффекта
    println("\n🔹 ТЕСТ 2: Проверка скользящего окна")
    testSlidingEffect(rateLimiter, userId, endpoint)
}

fun testSlidingEffect(rateLimiter: DistributedRateLimiter, userId: String, endpoint: String) {
    // Делаем несколько запросов и наблюдаем как окно "скользит"
    println("Делаем 8 быстрых запросов...")
    repeat(8) {
        rateLimiter.tryAcquire(userId, endpoint)
        Thread.sleep(200)
    }

    println("Ждём 3 секунды...")
    Thread.sleep(3000)

    println("Делаем ещё 5 запросов...")
    repeat(5) { i ->
        val result = rateLimiter.tryAcquire(userId, endpoint)
        println("  Запрос ${i + 1}: ${if (result.allowed) "✅" else "❌"} (${result.remaining} осталось)")
    }
}