package tv.codealong.tutorials.various.other

import java.util.concurrent.atomic.AtomicBoolean

/**
 * -=Идеальное решение для предзагрузки классов=-
 * Шаг 1: Создайте Utilities
 * Шаг 2: Extension для автоматической предзагрузки
 * Шаг 3: Использование в тестах
 * ```
 * // Способ 1: Аннотация на классе
 * @ExtendWith(ClassPreloadingExtension::class)
 * @Execution(ExecutionMode.CONCURRENT)
 * class MyConcurrentTest {
 *     // тесты
 * }
 *
 * // Способ 2: Глобальная регистрация в junit-platform.properties
 * junit.jupiter.extensions.autodetection.enabled=true
 * ```
 *
 * 🎯 Ключевые моменты:
 * - Используйте Extension - он безопаснее для параллельного выполнения
 * - Kotlin uses companion object вместо static блоков
 * - Предзагружайте Spring components, locks, utilities
 * - Делайте предзагрузку один раз на весь test run
 *
 * 🚀 Полный workflow предотвращения дедлоков:
 * - Предзагрузка всех критических классов в @BeforeAll
 * - Использование @Execution(ExecutionMode.CONCURRENT)
 * - Мониторинг работы ForkJoinPool
 * - Анализ оставшихся проблем через thread dump
 */
object PreloadUtils {


    private val preloaded = AtomicBoolean(false)

    fun preloadCriticalClasses() {
        // Атомарно присваивает значение newValue и возвращает старое значение с
        // эффектом памяти, как указано в VarHandle. getAndSet.
        if (preloaded.getAndSet(true)) return

        // 1. Spring компоненты
        preloadSpringClasses()

        // 2. Утилитные классы
        preloadUtilityClasses()

        // 3. Ваши классы со сложной инициализацией
        preloadYourClasses()
    }

    private fun preloadSpringClasses() {
        arrayOf(
            "org.springframework.context.annotation.Configuration",
            "org.springframework.stereotype.Service",
            "org.springframework.transaction.support.TransactionTemplate"
        ).forEach { safePreload(it) }
    }

    private fun preloadUtilityClasses() {
        arrayOf(
            "java.util.concurrent.locks.ReentrantLock",
            "java.util.concurrent.ConcurrentHashMap",
            "com.fasterxml.jackson.databind.ObjectMapper"
        ).forEach { safePreload(it) }
    }

    private fun preloadYourClasses() {
        // Ваши классы с companion object init блоками
        arrayOf(
            "com.yourproject.config.DatabaseConfig",
            "com.yourproject.utils.ValidationUtils",
            "com.yourproject.services.*Service" // Паттерн
        ).forEach { safePreload(it) }
    }

    private fun safePreload(className: String) {
        try {
            if (className.endsWith(".*")) {
                preloadPackage(className.removeSuffix(".*"))
            } else {
                Class.forName(className)
            }
        } catch (e: Exception) {
            // Игнорируем - класс может не быть в classpath
        }
    }

    private fun preloadPackage(packageName: String) {
        // Упрощенная реализация - можно использовать Reflection
        println("Preloading package: $packageName")
    }
}