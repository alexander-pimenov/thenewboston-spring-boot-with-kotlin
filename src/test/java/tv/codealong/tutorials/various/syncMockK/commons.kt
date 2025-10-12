package tv.codealong.tutorials.various.syncMockK

import io.mockk.mockk
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

object MockLock {
    val lock = ReentrantLock() // или ReentrantLock(true) для честности (FIFO) - потоки получают доступ в порядке очереди
}

// Компромиссный вариант - с таймаутом, но большим
inline fun <reified T> syncMockK(name: String? = null, relaxed: Boolean = false): T {
    // 10 секунд - достаточно для любых моков
    // 1. Захватываем lock
    if (!MockLock.lock.tryLock(10, TimeUnit.SECONDS)) {
        throw IllegalStateException("Mock creation timeout - possible deadlock")
    }
    // Всегда используйте try-finally!
    try {
        return mockk(name = name, relaxed = relaxed) // 2. Создаем мок и "запоминаем" результат и "готовим" к возврату
        // но ...сначала выполнится finally! а потом возвращаем значение
    } finally {
        MockLock.lock.unlock() // 3. ОСВОБОЖДАЕМ LOCK! ✅ ГАРАНТИРОВАННО освобождаем!
    }
    // 4. Возвращаем результат
}

/**
 * Пример функции с локом и таймаутом, в которой что-то выполняется
 */
fun tryDoSomething(): Boolean {
    return if (MockLock.lock.tryLock(1, TimeUnit.SECONDS)) { // Ждем максимум 1 секунду
        try {
            // Критическая секция, тут выполняется что-то
            true // Успех
        } finally {
            MockLock.lock.unlock()
        }
    } else {
        false // Не удалось получить lock
    }
}