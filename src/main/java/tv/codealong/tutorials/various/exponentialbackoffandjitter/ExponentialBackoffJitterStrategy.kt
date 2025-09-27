package tv.codealong.tutorials.various.exponentialbackoffandjitter

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.random.Random

fun exponentialBackoffWithJitter(attempt: Int): Long {
    val baseDelay = 100L
    val maxDelay = 2000L
    val expDelay = min(baseDelay * (1 shl attempt), maxDelay)
    val jitter = Random.nextLong(expDelay / 2, expDelay)
    return jitter
}

/**
 * Давай добавим **Jitter** к нашей стратегии **Exponential Backoff**, чтобы потоки не «синхронно»
 * конкурировали за ресурсы. Это особенно полезно, когда много потоков одновременно пытаются получить доступ —
 * случайный разброс помогает избежать повторных конфликтов.
 *
 * ---
 *
 * ### 🎲 Что такое Jitter?
 *
 * **Jitter** — это случайное отклонение от рассчитанного времени ожидания. Вместо того чтобы ждать строго 800 мс,
 * поток может ждать, например, от 600 до 1000 мс. Это делает поведение системы более гибким и устойчивым к перегрузке.
 *
 * ---
 *
 * ### 🧪 Пример: Exponential Backoff + Jitter в Kotlin
 *
 * ```kotlin
 * import java.util.concurrent.locks.ReentrantLock
 * import java.util.concurrent.TimeUnit
 * import kotlin.math.min
 * import kotlin.random.Random
 *
 * fun exponentialBackoffWithJitter(attempt: Int): Long {
 *     val baseDelay = 100L
 *     val maxDelay = 2000L
 *     val expDelay = min(baseDelay * (1 shl attempt), maxDelay)
 *     val jitter = Random.nextLong(expDelay / 2, expDelay)
 *     return jitter
 * }
 *
 * fun tryLockWithBackoffAndJitter(lock: ReentrantLock, threadName: String, maxAttempts: Int = 5) {
 *     var attempt = 0
 *     while (attempt < maxAttempts) {
 *         println("$threadName: попытка $attempt захватить блокировку")
 *         if (lock.tryLock(300, TimeUnit.MILLISECONDS)) {
 *             try {
 *                 println("$threadName: успешно захватил блокировку")
 *                 Thread.sleep(500)
 *                 break
 *             } finally {
 *                 lock.unlock()
 *                 println("$threadName: освободил блокировку")
 *             }
 *         } else {
 *             val waitTime = exponentialBackoffWithJitter(attempt)
 *             println("$threadName: не удалось захватить, ждём $waitTime мс")
 *             Thread.sleep(waitTime)
 *             attempt++
 *         }
 *     }
 *
 *     if (attempt == maxAttempts) {
 *         println("$threadName: не удалось захватить блокировку после $maxAttempts попыток")
 *     }
 * }
 *
 * fun main() {
 *     val lock = ReentrantLock()
 *
 *     val thread1 = Thread {
 *         tryLockWithBackoffAndJitter(lock, "Поток 1")
 *     }
 *
 *     val thread2 = Thread {
 *         tryLockWithBackoffAndJitter(lock, "Поток 2")
 *     }
 *
 *     thread1.start()
 *     thread2.start()
 * }
 * ```
 *
 * ---
 *
 * ### 🔍 Что делает этот код:
 * - Каждая задержка между попытками включает **случайный разброс**.
 * - Это снижает вероятность того, что потоки будут конкурировать одновременно.
 * - Поведение становится более «естественным» и устойчивым.
 *
 */
fun tryLockWithBackoffAndJitter(lock: ReentrantLock, threadName: String, maxAttempts: Int = 5) {
    var attempt = 0
    while (attempt < maxAttempts) {
        println("$threadName: попытка $attempt захватить блокировку")
        if (lock.tryLock(300, TimeUnit.MILLISECONDS)) {
            try {
                println("$threadName: успешно захватил блокировку")
                Thread.sleep(500)
                break
            } finally {
                lock.unlock()
                println("$threadName: освободил блокировку")
            }
        } else {
            val waitTime = exponentialBackoffWithJitter(attempt)
            println("$threadName: не удалось захватить, ждём $waitTime мс")
            Thread.sleep(waitTime)
            attempt++
        }
    }

    if (attempt == maxAttempts) {
        println("$threadName: не удалось захватить блокировку после $maxAttempts попыток")
    }
}

fun main() {
    val lock = ReentrantLock()

    val thread1 = Thread {
        tryLockWithBackoffAndJitter(lock, "Поток 1")
    }

    val thread2 = Thread {
        tryLockWithBackoffAndJitter(lock, "Поток 2")
    }

    thread1.start()
    thread2.start()
}
