package tv.codealong.tutorials.various.other

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

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
 * Первый вариант класса ClassPreloadingExtensionWithAtomic использует AtomicBoolean с методом compareAndSet,
 * который гарантирует атомарное изменение флага без явной блокировки, что проще, эффективнее и менее подвержено
 * ошибкам при многопоточном доступе.
 *
 * Второй вариант ClassPreloadingExtension реализует двойную проверку с блокировкой (double-checked locking) через
 * synchronized (в Kotlin - synchronized блока), что тоже корректно, но более громоздко и потенциально сложнее в
 * поддержке из-за обязательной работы с volatile переменной и блокировкой.
 *
 * ## Корректность и предпочтение
 *
 * - Оба варианта корректны с точки зрения многопоточности и предотвращения повторного выполнения загрузки.
 * - Первый вариант ClassPreloadingExtensionWithAtomic более идиоматичен и минималистичен для подобных задач в Kotlin/Java, благодаря AtomicBoolean.
 * - Второй вариант ClassPreloadingExtension имеет больше кода и может быть менее эффективен из-за синхронизации, хотя также безопасен.
 *
 * **Итог**: Первый вариант ClassPreloadingExtensionWithAtomic считается более корректным и предпочтительным для
 * задачи ленивой одноразовой инициализации. Он проще, лаконичнее и эффективнее управляет флагом состояния без лишних блокировок.
 */
class ClassPreloadingExtension : BeforeAllCallback {

    companion object {
        private var preloaded = false // ← Без @Volatile!
        private val lock = Any()
    }

    /**
     * Судя по логам, это выполняется один раз и самым первым:
     * ⏳ Preloading classes...
     * ✅ Classes preloaded
     *
     *
     * Как работает double-check:
     * Поток 1: if (!preloaded) → false → входит в synchronized
     * Поток 2: if (!preloaded) → ждет synchronized
     *
     * Поток 1: synchronized { if (!preloaded) → true } → устанавливает preloaded = true
     * Поток 1: выходит из synchronized
     *
     * Поток 2: входит в synchronized → if (!preloaded) → false → выходит
     */
    override fun beforeAll(context: ExtensionContext) {
        if (!preloaded) {                                   // ← Чтение 1 ← Быстрая проверка без синхронизации
            synchronized(lock) {                            // ← Синхронизация ← Синхронизация для точной проверки
                if (!preloaded) {                           // ← Чтение 2 (под синхронизацией)
                    println("⏳ Preloading classes...")
                    PreloadUtils.preloadCriticalClasses()
                    preloaded = true                        // ← Запись (под синхронизацией)
                    println("✅ Classes preloaded")
                }
            }
        }
    }
}