package tv.codealong.tutorials.various.yandex.task2_1

import java.time.Instant

/**
 * 🧪 ТЕСТИРУЕМ FIXED WINDOW
 */
fun main() {
    testFixedWindow()
}

fun testFixedWindow() {
    println("=== 🪟 ТЕСТИРОВАНИЕ FIXED WINDOW ===")

    val rateLimiter = DistributedRateLimiter()

    // Настраиваем Fixed Window: 5 запросов в 3 секунды
    rateLimiter.configureEndpoint(
        "/api/feed",
        RateLimitConfig(
            maxRequests = 5,
            windowSize = java.time.Duration.ofSeconds(3),
            algorithm = RateLimitAlgorithm.FIXED_WINDOW
        )
    )

    val userId = "user456"
    val endpoint = "/api/feed"

    // Тест 1: Быстрые запросы в пределах лимита
    println("🔹 ТЕСТ 1: 6 быстрых запросов (лимит: 5/3сек)")
    repeat(6) { i ->
        val result = rateLimiter.tryAcquire(userId, endpoint)
        println(
            "Запрос ${i + 1}: ${if (result.allowed) "✅" else "❌"} " +
                    "- ${result.remaining} осталось, " +
                    "окно сбрасывается через ${java.time.Duration.between(Instant.now(), result.resetTime).seconds}с"
        )
        Thread.sleep(100)
    }

    // Тест 2: Ждём смены окна
    println("\n⏳ Ждём 3.5 секунды для смены окна...")
    Thread.sleep(3500)

    println("🔹 ТЕСТ 2: Запросы после смены окна")
    repeat(3) { i ->
        val result = rateLimiter.tryAcquire(userId, endpoint)
        println(
            "Запрос ${i + 1}: ${if (result.allowed) "✅" else "❌"} " +
                    "- ${result.remaining} осталось"
        )
    }

    // Тест 3: Edge case - запросы на границе окон
    println("\n🔹 ТЕСТ 3: Запросы на границе окон")
    testWindowBoundary(rateLimiter)
}

fun testWindowBoundary(rateLimiter: DistributedRateLimiter) {
    val userId = "edgeUser"
    val endpoint = "/api/feed"

    // Делаем запросы в конце окна
    println("Делаем 4 запроса...")
    repeat(4) {
        rateLimiter.tryAcquire(userId, endpoint)
        Thread.sleep(100)
    }

    // Ждём немного, но не до конца окна
    Thread.sleep(2000)

    println("Ещё 2 запроса...")
    repeat(2) {
        val result = rateLimiter.tryAcquire(userId, endpoint)
        println("Результат: ${if (result.allowed) "✅" else "❌"} (${result.remaining} осталось)")
    }

    // Ждём смены окна
    Thread.sleep(1500)

    println("Запрос после смены окна:")
    val result = rateLimiter.tryAcquire(userId, endpoint)
    println("Результат: ${if (result.allowed) "✅" else "❌"} (${result.remaining} осталось)")
}