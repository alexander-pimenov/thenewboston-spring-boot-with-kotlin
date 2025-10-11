package tv.codealong.tutorials.various.yandex.task2_1

import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 🚪 СОСТОЯНИЕ SLIDING WINDOW ДЛЯ ОДНОГО ПОЛЬЗОВАТЕЛЯ НА ОДНОМ ENDPOINT
 * 🎯 Sliding Window Algorithm
 * Принцип работы:
 * - Учитываем запросы за последние N секунд/минут
 * - Используем счётчики для небольших под-окон (сегментов)
 * - Точнее чем Fixed Window, нет проблем с границами окон
 * - Более сложная реализация, требует больше памяти
 *
 * 🎯 ОСОБЕННОСТИ SLIDING WINDOW:
 * ✅ Преимущества:
 * - Высокая точность - точно учитывает последние N секунд
 * - Нет проблем с границами - равномерное распределение нагрузки
 * - Справедливость - пользователи не могут "обмануть" систему
 *
 * ⚠️ Недостатки:
 * - Сложность реализации - нужно управлять сегментами
 * - Больше потребление памяти - храним несколько сегментов
 * - Вычислительная сложность - нужно суммировать сегменты
 *
 * 💡 Оптимизации в нашей реализации:
 * - ConcurrentSkipListMap - быстрый доступ и очистка старых сегментов
 * - Атомарные счётчики - thread-safe операции
 * - Автоматическая очистка - удаляем устаревшие сегменты
 */
class SlidingWindowState : RateLimitState {

    // Храним сегменты: timestamp -> количество запросов
    private val segments = ConcurrentSkipListMap<Long, AtomicInteger>()

    override var lastAccess: Instant = Instant.now()

    //fun getLastAccess(): Instant = lastAccess

    override fun tryAcquire(config: RateLimitConfig): RateLimitResult {
        val now = Instant.now()
        lastAccess = now

        val currentTime = now.toEpochMilli()
        val windowSizeMillis = config.windowSize.toMillis()
        val segmentSize = calculateSegmentSize(windowSizeMillis)

        // 1. Очищаем устаревшие сегменты
        cleanupOldSegments(currentTime, windowSizeMillis)

        // 2. Определяем текущий сегмент
        val currentSegment = (currentTime / segmentSize) * segmentSize

        // 3. Увеличиваем счётчик текущего сегмента
        val currentCount = segments
            .getOrPut(currentSegment) { AtomicInteger(0) }
            .incrementAndGet()

        // 4. Считаем общее количество запросов в окне
        val totalRequests = segments.values.sumOf { it.get() }

        val allowed = totalRequests <= config.maxRequests
        val remaining = maxOf(0, config.maxRequests - totalRequests)

        // 5. Рассчитываем время сброса (когда самый старый сегмент выйдет из окна)
        val resetTime = calculateResetTime(currentTime, windowSizeMillis)

        return RateLimitResult(
            allowed = allowed,
            remaining = remaining,
            resetTime = resetTime,
            limit = config.maxRequests,
            algorithm = config.algorithm
        )
    }

    private fun calculateSegmentSize(windowSizeMillis: Long): Long {
        // Делим окно на 10 сегментов для баланса точности и производительности
        return maxOf(1000, windowSizeMillis / 10) // минимум 1 секунда
    }

    private fun cleanupOldSegments(currentTime: Long, windowSizeMillis: Long) {
        val oldestAllowed = currentTime - windowSizeMillis

        // Удаляем сегменты старше окна
        segments.headMap(oldestAllowed).clear()
    }

    private fun calculateResetTime(currentTime: Long, windowSizeMillis: Long): Instant {
        // Время сброса = текущее время + (время до выхода самого старого сегмента из окна)
        val oldestSegment = segments.firstKeyOrNull()
        return if (oldestSegment != null) {
            val resetTimeMillis = oldestSegment + windowSizeMillis
            Instant.ofEpochMilli(resetTimeMillis)
        } else {
            Instant.ofEpochMilli(currentTime + windowSizeMillis)
        }
    }

    // Вспомогательная функция для безопасного получения первого ключа
    private fun ConcurrentSkipListMap<Long, AtomicInteger>.firstKeyOrNull(): Long? {
        return try {
            firstKey()
        } catch (e: NoSuchElementException) {
            null
        }
    }
}