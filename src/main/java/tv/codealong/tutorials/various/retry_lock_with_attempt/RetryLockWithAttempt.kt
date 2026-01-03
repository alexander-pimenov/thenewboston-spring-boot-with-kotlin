package tv.codealong.tutorials.various.retry_lock_with_attempt

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit

class Resource(val name: String, val lock: ReentrantLock = ReentrantLock())


fun main() {
    val resourceA = Resource("ResourceA")
    val resourceB = Resource("ResourceB")

    val thread1 = Thread {
        retryLock(resourceA, resourceB, "Thread 1")
    }

    val thread2 = Thread {
        retryLock(resourceB, resourceA, "Thread 2")
    }

    thread1.start()
    thread2.start()
}

/**
 * 📋 Что делает этот код:
 * - Пытается захватить оба ресурса до maxAttempts раз.
 * - Логирует каждую попытку.
 * - Делает паузу между попытками, чтобы снизить нагрузку.
 * - Если не удалось — сообщает об ошибке.
 *
 * Такой подход особенно полезен в системах с высокой конкуренцией за ресурсы.
 */
fun retryLock(first: Resource, second: Resource, threadName: String, maxAttempts: Int = 5) {
    var attempt = 1
    while (attempt <= maxAttempts) {
        println("$threadName: Попытка $attempt to lock ${first.name} and ${second.name}")
        try {
            if (first.lock.tryLock(300, TimeUnit.MILLISECONDS)) {
                try {
                    println("$threadName: locked ${first.name}")
                    Thread.sleep(100)
                    if (second.lock.tryLock(300, TimeUnit.MILLISECONDS)) {
                        try {
                            println("$threadName: locked ${second.name}")
                            println("$threadName: working with both resources")
                            break // успех — выходим из цикла
                        } finally {
                            second.lock.unlock()
                            println("$threadName: released ${second.name}")
                        }
                    } else {
                        println("$threadName: could not lock ${second.name}, retrying...")
                    }
                } finally {
                    first.lock.unlock()
                    println("$threadName: released ${first.name}")
                }
            } else {
                println("$threadName: could not lock ${first.name}, retrying...")
            }
        } catch (e: InterruptedException) {
            println("$threadName: interrupted")
            break
        }

        attempt++
        Thread.sleep(200) // пауза перед следующей попыткой
    }

    if (attempt > maxAttempts) {
        println("$threadName: failed to acquire both locks after $maxAttempts attempts")
    }
}
