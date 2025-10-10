package tv.codealong.tutorials.various.yandex.task2_1

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * 🎯 Что мы использовали для хранения:
 * - ConcurrentHashMap - для конфигураций и состояний пользователей
 * - AtomicReference - для thread-safe обновления состояния bucket'а
 * - AtomicLong - для атомарных метрик
 *
 * 🔍 Ключевые особенности реализации:
 * ✅ Thread-safe - используем атомарные операции
 * ✅ Эффективное использование памяти - чистим неактивных пользователей
 * ✅ Гибкая конфигурация - разные лимиты для разных endpoint'ов
 * ✅ Метрики - мониторинг использования
 * ✅ Производительность - минимальные блокировки
 */
// 🎯 БАЗОВАЯ РЕАЛИЗАЦИЯ
class DistributedRateLimiter : RateLimiter {

    // Хранилище конфигураций: endpoint -> config
    private val endpointConfigs = ConcurrentHashMap<String, RateLimitConfig>()

    // Хранилище состояний пользователей: userId -> endpoint -> State
    private val userStates = ConcurrentHashMap<String, ConcurrentHashMap<String, RateLimitState>>()

    // Метрики: userId -> metrics
    private val metricsMap = ConcurrentHashMap<String, AtomicRateLimitMetrics>()

    // Scheduled executor для cleanup'а
    private val scheduler = Executors.newScheduledThreadPool(1)

    init {
        // Запускаем периодическую очистку устаревших данных
        scheduler.scheduleAtFixedRate({ cleanUp() }, 1, 1, TimeUnit.HOURS)
    }

    override fun tryAcquire(userId: String, endpoint: String): RateLimitResult {
        val config = endpointConfigs[endpoint] ?: return createUnlimitedResult()

        val userEndpointStates = userStates.getOrPut(userId) { ConcurrentHashMap() }
        val state = userEndpointStates.getOrPut(endpoint) { createState(config) }

        val result = state.tryAcquire(config)

        // Обновляем метрики
        updateMetrics(userId, result.allowed)

        return result
    }

    override fun getMetrics(userId: String): RateLimitMetrics? {
        return metricsMap[userId]?.toRateLimitMetrics(userId)
    }

    override fun cleanUp() {
        val now = Instant.now()
        // Удаляем неактивных пользователей (неактивность > 24 часа)
        userStates.entries.removeAll { (userId, states) ->
            val lastActivity = states.values.maxOfOrNull { it.lastAccess } ?: now
            now.isAfter(lastActivity.plusSeconds(24 * 60 * 60))
        }
        // Также чистим метрики неактивных пользователей
        metricsMap.entries.removeAll { (userId, _) ->
            !userStates.containsKey(userId)
        }
    }

    // 🔧 Вспомогательные методы
    private fun createUnlimitedResult(): RateLimitResult {
        return RateLimitResult(
            allowed = true,
            remaining = Int.MAX_VALUE,
            limit = Int.MAX_VALUE,
            algorithm = RateLimitAlgorithm.FIXED_WINDOW
        )
    }

    /**
     * 🏗️ Фабрика состояний
     *
     * Создает состояние для разных алгоритмов
     */
    private fun createState(config: RateLimitConfig): RateLimitState {
        return when (config.algorithm) {
            RateLimitAlgorithm.TOKEN_BUCKET -> TokenBucketState()
            RateLimitAlgorithm.FIXED_WINDOW -> FixedWindowState()
            RateLimitAlgorithm.SLIDING_WINDOW -> SlidingWindowState()
        }
    }

    private fun updateMetrics(userId: String, allowed: Boolean) {
        val metrics = metricsMap.getOrPut(userId) { AtomicRateLimitMetrics() }
        if (allowed) {
            metrics.incrementSuccess()
        } else {
            metrics.incrementBlocked()
        }
    }

    // Конфигурация endpoints, для настройки лимитов для разных endpoint'ов
    fun configureEndpoint(endpoint: String, config: RateLimitConfig) {
        endpointConfigs[endpoint] = config
    }

    // Получение всех конфигураций (для мониторинга)
    fun getEndpointConfigs(): Map<String, RateLimitConfig> {
        return endpointConfigs.toMap()
    }

    // Динамическое изменение лимитов
    fun updateEndpointConfig(endpoint: String, newConfig: RateLimitConfig) {
        endpointConfigs[endpoint] = newConfig
        // Можно добавить логику для сброса состояний при изменении конфигурации
    }
}

// 🎯 ИНТЕРФЕЙС ДЛЯ СОСТОЯНИЙ
interface RateLimitState {
    val lastAccess: Instant
    fun tryAcquire(config: RateLimitConfig): RateLimitResult
}

// 📊 АТОМАРНЫЕ МЕТРИКИ (thread-safe)
class AtomicRateLimitMetrics {
    private val totalRequests = java.util.concurrent.atomic.AtomicLong(0)
    private val blockedRequests = java.util.concurrent.atomic.AtomicLong(0)

    fun incrementSuccess() {
        totalRequests.incrementAndGet()
    }

    fun incrementBlocked() {
        totalRequests.incrementAndGet()
        blockedRequests.incrementAndGet()
    }

    fun toRateLimitMetrics(userId: String): RateLimitMetrics {
        val total = totalRequests.get()
        val blocked = blockedRequests.get()
        val success = total - blocked

        return RateLimitMetrics(
            userId = userId,
            totalRequests = total,
            blockedRequests = blocked,
            successRate = if (total > 0) success.toDouble() / total.toDouble() else 1.0,
            lastActivity = Instant.now()
        )
    }
}