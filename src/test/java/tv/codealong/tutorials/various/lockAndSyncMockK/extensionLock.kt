package tv.codealong.tutorials.various.lockAndSyncMockK

import io.mockk.mockk
import java.util.concurrent.locks.ReentrantLock

/**
 * 🎯 Когда выбрать что?
 * ✅ synchronized - когда:
 * - Простая синхронизация
 * - Не нужны advanced фичи
 * - Хочется простоты и читаемости
 * - Автоматическое управление памятью
 *
 * ✅ ReentrantLock - когда:
 * - Нужны таймауты (избежание дедлоков)
 * - Нужна прерываемость ожидания
 * - Требуется честное распределение lock'ов
 * - Нужен try-lock без блокировки
 * - Сложная логика с условными переменными
 */
// Создаем удобные extension-функции, который уже содержит try-finally
fun <T> ReentrantLock.withLock(action: () -> T): T {
    lock()
    // Всегда используйте try-finally!
    try {
        return action()
    } finally {
        unlock()
    }
}

// Использование
fun example() {
    MockLock.lock.withLock {
        val mock = mockk<MyService>()
        // работа с mock
    }
}

class MyService {

}
