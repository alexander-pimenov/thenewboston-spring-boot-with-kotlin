package tv.codealong.tutorials.various.other

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 1. Что лучше: Extension или Object?
 * ClassPreloadingExtension - лучше!
 * Почему: Он гарантирует однократное выполнение на весь test run.
 * Object ClassPreloader - риск двойного выполнения
 * Может вызваться несколько раз в параллельных тестах.
 *
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
 * В этом варианте еще проще, лаконичнее и эффективнее - ленивая инициализация, и она сработает ровно один раз при
 * первом обращении к полю initialized.
 */
class ClassPreloadingExtensionWithLazy : BeforeAllCallback {

    companion object {
        private val initialized by lazy {
            PreloadUtils.preloadCriticalClasses()
            true
        }
    }


    /**
     * Судя по логам, это выполняется один раз и самым первым:
     * ⏳ Preloading classes...
     * ✅ Classes preloaded
     *
     *
     * Тут работает ленивая инициализация - произойдет один раз, как только обратятся к полю initialized.
     */
    override fun beforeAll(context: ExtensionContext) {
        initialized // Просто обращаемся - инициализация будет один раз
    }
}