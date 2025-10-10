package tv.codealong.tutorials.various.yandex.task2_1

import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min


/**
 *
 * СОСТОЯНИЕ TOKEN BUCKET ДЛЯ ОДНОГО ПОЛЬЗОВАТЕЛЯ НА ОДНОМ ENDPOINT
 *
 * 🎯 Token Bucket Algorithm
 * Принцип работы:
 * - Есть "ведро" с токенами (максимальная ёмкость = maxRequests)
 * - Токены добавляются с постоянной скоростью
 * - Каждый запрос забирает 1 токен
 * - Если токены закончились - запрос блокируется
 */
class TokenBucketState : RateLimitState {

    private data class BucketState(
        val tokens: Double,           // Текущее количество токенов
        val lastRefillTime: Instant   // Время последнего пополнения
    )

    private val state = AtomicReference<BucketState>(
        BucketState(tokens = 0.0, lastRefillTime = Instant.now())
    )

    override val lastAccess: Instant
        get() = state.get().lastRefillTime

    override fun tryAcquire(config: RateLimitConfig): RateLimitResult {
        val now = Instant.now()
        val updatedState = state.updateAndGet { currentState ->
            // 1. Пополняем токены based on time passed
            val timePassed = java.time.Duration.between(currentState.lastRefillTime, now).toMillis()
            val refillAmount = (timePassed / config.windowSize.toMillis()) * config.maxRequests

            val newTokens = min(
                config.maxRequests.toDouble(), // Не больше максимума
                currentState.tokens + refillAmount
            )

            // 2. Пытаемся взять токен
            val tokensAfterAcquire = newTokens - 1.0

            BucketState(
                tokens = tokensAfterAcquire,
                lastRefillTime = now
            )
        }

        val allowed = updatedState.tokens >= 0
        val remaining = maxOf(0, updatedState.tokens.toInt())

        return RateLimitResult(
            allowed = allowed,
            remaining = remaining,
            resetTime = calculateResetTime(now, config),
            limit = config.maxRequests,
            algorithm = config.algorithm
        )
    }

    private fun calculateResetTime(now: Instant, config: RateLimitConfig): Instant {
        // Время, когда ведро полностью наполнится
        return now.plus(config.windowSize)
    }
}