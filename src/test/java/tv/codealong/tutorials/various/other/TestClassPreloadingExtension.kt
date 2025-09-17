package tv.codealong.tutorials.various.other

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Extension для использования
 *
 * Ключевые особенности:
 * - Рекурсивная предзагрузка - загружает все связанные классы
 * - Безопасная обработка - игнорирует ошибки
 * - Предотвращение дублирования - отслеживает уже загруженные классы
 * - Generic types support - обрабатывает параметризованные типы
 * - Статистика - показывает что было загружено
 */
class TestClassPreloadingExtension : BeforeAllCallback {

    companion object {
        private val preloaded = AtomicBoolean(false)
    }

    override fun beforeAll(context: ExtensionContext) {
        // Предзагружаем только один раз для этого класса
        if (preloaded.compareAndSet(false, true)) {
            PreloadUtilsExtended.preloadClassesFor(context)
        }
    }
}