package tv.codealong.tutorials.various.yandex.task2_1

import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

/**
 * СОСТОЯНИЕ FIXED WINDOW ДЛЯ ОДНОГО ПОЛЬЗОВАТЕЛЯ НА ОДНОМ ENDPOINT
 *
 * 🎯 Fixed Window Algorithm
 * Принцип работы:
 * - Время разбивается на фиксированные окна (например, каждая минута)
 * - Считаем запросы внутри каждого окна
 * - Когда окно заканчивается - счётчик сбрасывается
 * - Простота реализации, но возможны всплески на границах окон
 *
 * 🎯 ОСОБЕННОСТИ FIXED WINDOW:
 * ✅ Преимущества:
 * - Простота реализации - минимальная логика
 * - Низкие накладные расходы - только счётчик и временная метка
 * - Предсказуемость - легко понять когда сбросится лимит
 *
 * ⚠️ Недостатки:
 * - Всплески на границах - пользователь может сделать 2N запросов за 2 окна
 * - Менее точный чем sliding window
 */
class FixedWindowState : RateLimitState {

    private data class WindowState(
        val count: Int,              // Количество запросов в текущем окне
        val windowStart: Instant,    // Начало текущего окна
        val lastAccess: Instant      // Последний доступ (для cleanup)
    )

    private val state = AtomicReference<WindowState>(
        WindowState(count = 0, windowStart = Instant.now(), lastAccess = Instant.now())
    )

    override val lastAccess: Instant
        get() = state.get().lastAccess

    override fun tryAcquire(config: RateLimitConfig): RateLimitResult {
        val now = Instant.now()

        val updatedState = state.updateAndGet { currentState ->
            val currentWindowStart = currentState.windowStart
            val windowSizeMillis = config.windowSize.toMillis()

            // Проверяем, не сменилось ли окно
            val currentTimeMillis = now.toEpochMilli()
            val windowStartMillis = currentWindowStart.toEpochMilli()

            if (currentTimeMillis - windowStartMillis >= windowSizeMillis) {
                // Новое окно - сбрасываем счётчик
                WindowState(
                    count = 1,
                    windowStart = calculateWindowStart(now, config),
                    lastAccess = now
                )
            } else {
                // Текущее окно - увеличиваем счётчик
                WindowState(
                    count = currentState.count + 1,
                    windowStart = currentWindowStart,
                    lastAccess = now
                )
            }
        }

        val allowed = updatedState.count <= config.maxRequests
        val remaining = maxOf(0, config.maxRequests - updatedState.count)
        val resetTime = calculateResetTime(updatedState.windowStart, config)

        return RateLimitResult(
            allowed = allowed,
            remaining = remaining,
            resetTime = resetTime,
            limit = config.maxRequests,
            algorithm = config.algorithm
        )
    }

    private fun calculateWindowStart(now: Instant, config: RateLimitConfig): Instant {
        val windowSizeMillis = config.windowSize.toMillis()
        val currentMillis = now.toEpochMilli()
        val windowStartMillis = (currentMillis / windowSizeMillis) * windowSizeMillis
        return Instant.ofEpochMilli(windowStartMillis)
    }

    private fun calculateResetTime(windowStart: Instant, config: RateLimitConfig): Instant {
        return windowStart.plus(config.windowSize)
    }
}