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
 * Первый вариант класса использует AtomicBoolean с методом compareAndSet, который гарантирует атомарное
 * изменение флага без явной блокировки, что проще, эффективнее и менее подвержено ошибкам при многопоточном доступе.
 *
 * Второй вариант реализует двойную проверку с блокировкой (double-checked locking) через synchronized (в
 * Kotlin - synchronized блока), что тоже корректно, но более громоздко и потенциально сложнее в поддержке из-за
 * обязательной работы с volatile переменной и блокировкой.
 *
 * ## Корректность и предпочтение
 *
 * - Оба варианта ClassPreloadingExtensionWithAtomic и ClassPreloadingExtension корректны с точки зрения многопоточности
 *   и предотвращения повторного выполнения загрузки.
 * - Первый вариант ClassPreloadingExtensionWithAtomic более идиоматичен и минималистичен для подобных задач в Kotlin/Java, благодаря AtomicBoolean.
 * - Второй вариант ClassPreloadingExtension имеет больше кода и может быть менее эффективен из-за синхронизации, хотя также безопасен.
 *
 * **Итог**: Первый вариант считается более корректным и предпочтительным для задачи ленивой одноразовой инициализации.
 * Он проще, лаконичнее и эффективнее управляет флагом состояния без лишних блокировок .
 */
class ClassPreloadingExtensionWithAtomic : BeforeAllCallback {

    companion object {
        private val preloaded = AtomicBoolean(false)
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
     *
     *
     * Механизм `preloaded.compareAndSet(false, true)` — это атомарная операция из класса AtomicBoolean в Java/Kotlin.
     *
     * ### Что делает compareAndSet?
     *
     * Метод `compareAndSet(ожидаемоеЗначение, новоеЗначение)` атомарно проверяет, равно ли текущее значение переменной
     * ожидаемому (в данном случае false). Если да, то он изменяет значение на новое (true) и возвращает true. Если
     * текущее значение не равно ожидаемому, ничего не меняет и возвращает false.
     * Это значит, что операция происходит как единое атомарное действие без риска прерывания или состояния гонки между потоками.
     *
     * ### Для чего используется?
     *
     * В вашем примере это гарантирует, что блок кода с предзагрузкой классов выполнится `ровно один раз` независимо от
     * количества потоков, которые могут одновременно вызвать beforeAll(). Первый поток, успешно сменивший значение с
     * false на true, запускает метод предзагрузки. Все остальные потоки увидят true и пропустят этот блок.
     *
     * ### Преимущества:
     *
     * - Безопасность при работе с несколькими потоками.
     * - Отсутствие необходимости в явных блокировках (synchronized).
     * - Высокая производительность благодаря атомарным примитивам.
     *
     * ### Класс AtomicBoolean:
     *
     * Это обертка над булевским значением с атомарными операциями чтения и записи, а также методами сравнения и замены
     * значения atomically, обеспечивающая корректное взаимодействие между потоками.
     * Таким образом, preloaded.compareAndSet(false, true) — это эффективный и простой способ реализовать однократную
     * инициализацию с потокобезопасностью без блокировок.
     */
    override fun beforeAll(context: ExtensionContext) {
        if (preloaded.compareAndSet(false, true)) { // ← Чтение 1 ← Быстрая проверка
            println("⏳ Preloading classes...")
            PreloadUtils.preloadCriticalClasses()
            println("✅ Classes preloaded")
        }
    }
}