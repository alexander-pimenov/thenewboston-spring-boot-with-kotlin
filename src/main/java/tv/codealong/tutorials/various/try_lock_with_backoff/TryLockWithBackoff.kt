package tv.codealong.tutorials.various.try_lock_with_backoff

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import kotlin.math.min

fun exponentialBackoff(attempt: Int): Long {
    val baseDelay = 100L // базовая задержка в мс
    val maxDelay = 2000L // максимальная задержка
    return min(baseDelay * (1 shl attempt), maxDelay)
}

/**
 * 🧠 Что делает этот код:
 * - Поток пытается захватить блокировку с таймаутом.
 * - Если не удалось — ждёт экспоненциально увеличивающееся время.
 * - Повторяет попытки до maxAttempts.
 * - Это снижает конкуренцию и вероятность взаимной блокировки.
 */
fun tryLockWithBackoff(lock: ReentrantLock, threadName: String, maxAttempts: Int = 5) {
    var attempt = 0
    while (attempt < maxAttempts) {
        println("$threadName: попытка $attempt захватить блокировку")
        if (lock.tryLock(300, TimeUnit.MILLISECONDS)) {
            try {
                println("$threadName: успешно захватил блокировку")
                Thread.sleep(500) // имитируем работу
                break
            } finally {
                lock.unlock()
                println("$threadName: освободил блокировку")
            }
        } else {
            val waitTime = exponentialBackoff(attempt)
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
        tryLockWithBackoff(lock, "Поток 1")
    }

    val thread2 = Thread {
        tryLockWithBackoff(lock, "Поток 2")
    }

    thread1.start()
    thread2.start()
}
