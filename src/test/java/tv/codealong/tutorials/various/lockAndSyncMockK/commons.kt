package tv.codealong.tutorials.various.lockAndSyncMockK

import io.mockk.mockk
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock


object MockMonitor // Общий монитор для синхронизации

/**
 * 🎯 Почему это помогает от дедлоков:
 * - Синхронизация доступа к ClassLoader - основная причина дедлоков в параллельных тестах с моками
 * - MockK и трансформация байткода - при создании моков MockK использует инструментацию и ClassLoader'ы
 * - Параллельный доступ - без синхронизации несколько потоков одновременно пытаются загрузить/трансформировать классы
 *
 * 🏆 Преимущества решения:
 * - Простота и понятность - код легко читать и поддерживать
 * - Централизованная синхронизация - один объект для всех созданий моков - object MockMonitor
 * - Type-safe - благодаря reified T сохраняется типобезопасность
 * - Минимальные изменения в коде с тестами - нужно только заменить mockk() на syncMockK()
 */
inline fun <reified T : Any> syncMockK(): T {
    println("Thread ${Thread.currentThread().name} waiting for mock creation of ${T::class.java.simpleName}")
    synchronized(MockMonitor) { // Синхронизация по общему объекту
        println("Thread ${Thread.currentThread().name} creating mock of ${T::class.java.simpleName}")
        // Создание мока внутри synchronized блока
        return mockk<T>().also {
            println("Thread ${Thread.currentThread().name} created mock of ${T::class.java.simpleName}")
        }
    }
}

/**
 * 1. Более гранулярная синхронизация (если потребуется)
 */
val classLocks = ConcurrentHashMap<Class<*>, Any>()

inline fun <reified T> syncMockKGranular(): T {
    val classLock = classLocks.computeIfAbsent(T::class.java) { Any() }
    synchronized(classLock) {
        return mockk()
    }
}

/**
 * Рассмотрите кеширование моков если создание тяжелое:
 */
val mockCache = ConcurrentHashMap<Class<*>, Any>()

inline fun <reified T : Any> cachedSyncMockK(): T {
    return mockCache.computeIfAbsent(T::class.java) {
        synchronized(MockMonitor) {
            mockk<T>()
        }
    } as T
}



object MockLock {
    val lock =
        ReentrantLock() // или ReentrantLock(true) для честности (FIFO) - потоки получают доступ в порядке очереди
}

// Компромиссный вариант - с таймаутом, но большим
inline fun <reified T> syncMockK(
    name: String? = null,
    relaxed: Boolean = false,
    relaxUnitFun: Boolean = false,
): T {
    // 10 секунд - достаточно для любых моков
    // 1. Захватываем lock
    if (!MockLock.lock.tryLock(10, TimeUnit.SECONDS)) {
        throw IllegalStateException("Mock creation timeout - possible deadlock")
    }
    // Всегда используйте try-finally!
    try {
        // 2. Создаем мок и "запоминаем" результат и "готовим" к возврату
        // но ...сначала выполнится finally! а потом возвращаем значение
        return mockk(
            name = name,
            relaxed = relaxed,
            relaxUnitFun = relaxUnitFun
        )
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

fun main() {
    // Использование
    val mock = syncMockK<MyService>(relaxed = false)
}