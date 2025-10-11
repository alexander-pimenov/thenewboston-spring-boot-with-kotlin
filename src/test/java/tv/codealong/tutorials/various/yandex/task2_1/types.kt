package tv.codealong.tutorials.various.yandex.task2_1

// 📊 Модели данных
data class RateLimitConfig(
    val maxRequests: Int,
    val windowSize: java.time.Duration,
    val algorithm: RateLimitAlgorithm,
)

enum class RateLimitAlgorithm {
    TOKEN_BUCKET, FIXED_WINDOW, SLIDING_WINDOW
}

data class RateLimitResult(
    val allowed: Boolean,           //разрешено
    val remaining: Int,             //оставшееся
    val resetTime: java.time.Instant? = null,
    val limit: Int,
    val algorithm: RateLimitAlgorithm,  //алгоритм
)

// 🎯 Основной интерфейс
interface RateLimiter {
    fun tryAcquire(userId: String, endpoint: String): RateLimitResult
    fun getMetrics(userId: String): RateLimitMetrics?
    fun cleanUp()
}

// 📈 Метрики
data class RateLimitMetrics(
    val userId: String,
    val totalRequests: Long,
    val blockedRequests: Long,
    val successRate: Double,
    val lastActivity: java.time.Instant,
)