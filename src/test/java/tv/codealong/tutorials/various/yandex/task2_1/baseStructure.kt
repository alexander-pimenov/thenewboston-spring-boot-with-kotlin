package tv.codealong.tutorials.various.yandex.task2_1

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.min

// 🎯 БАЗОВАЯ РЕАЛИЗАЦИЯ
class DistributedRateLimiter : RateLimiter {

    // Хранилище конфигураций: endpoint -> config
    private val endpointConfigs = ConcurrentHashMap<String, RateLimitConfig>()

    // Хранилище состояний пользователей: userId -> endpoint -> State
    private val userStates = ConcurrentHashMap<String, ConcurrentHashMap<String, RateLimitState>>()

    // Метрики
    private val metricsMap = ConcurrentHashMap<String, RateLimitMetrics>()

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

        return state.tryAcquire(config)
    }

    override fun getMetrics(userId: String): RateLimitMetrics? {
        return metricsMap[userId]
    }

    override fun cleanUp() {
        val now = Instant.now()
        // Удаляем неактивных пользователей (неактивность > 24 часа)
        userStates.entries.removeAll { (userId, states) ->
            val lastActivity = states.values.maxOfOrNull { it.lastAccess } ?: now
            now.isAfter(lastActivity.plusSeconds(24 * 60 * 60))
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

    private fun createState(config: RateLimitConfig): RateLimitState {
        return when (config.algorithm) {
            RateLimitAlgorithm.TOKEN_BUCKET -> TokenBucketState()
            RateLimitAlgorithm.FIXED_WINDOW -> FixedWindowState()
            RateLimitAlgorithm.SLIDING_WINDOW -> SlidingWindowState()
        }
    }

    // Конфигурация endpoints, для настройки лимитов для разных endpoint'ов
    fun configureEndpoint(endpoint: String, config: RateLimitConfig) {
        endpointConfigs[endpoint] = config
    }
}

// 🎯 ИНТЕРФЕЙС ДЛЯ СОСТОЯНИЙ
interface RateLimitState {
    val lastAccess: Instant
    fun tryAcquire(config: RateLimitConfig): RateLimitResult
}