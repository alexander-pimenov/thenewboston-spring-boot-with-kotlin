package tv.codealong.tutorials.various.exponential_backoff_and_jitter2

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.random.Random

data class Resource(val name: String, val lock: ReentrantLock = ReentrantLock())

fun exponentialBackoffWithJitter(attempt: Int): Long {
    val baseDelay = 100L
    val maxDelay = 2000L
    val expDelay = min(baseDelay * (1 shl attempt), maxDelay)
    return Random.nextLong(expDelay / 2, expDelay)
}

/**
 * Давай теперь встроим стратегию **Exponential Backoff с Jitter** в многопоточную систему, где потоки конкурируют
 * за **несколько ресурсов**. Это приближает нас к реальным сценариям — например, транзакции в базе данных или задачи в пуле потоков.
 *
 * ---
 *
 * ### 🧪 Пример: несколько ресурсов + Backoff + Jitter
 *
 * ```kotlin
 * import java.util.concurrent.locks.ReentrantLock
 * import java.util.concurrent.TimeUnit
 * import kotlin.math.min
 * import kotlin.random.Random
 *
 * data class Resource(val name: String, val lock: ReentrantLock = ReentrantLock())
 *
 * fun exponentialBackoffWithJitter(attempt: Int): Long {
 *     val baseDelay = 100L
 *     val maxDelay = 2000L
 *     val expDelay = min(baseDelay * (1 shl attempt), maxDelay)
 *     return Random.nextLong(expDelay / 2, expDelay)
 * }
 *
 * fun tryLockResources(resources: List<Resource>, threadName: String, maxAttempts: Int = 5) {
 *     var attempt = 0
 *     while (attempt < maxAttempts) {
 *         println("$threadName: попытка $attempt захватить ресурсы ${resources.map { it.name }}")
 *
 *         val acquired = mutableListOf<Resource>()
 *         try {
 *             var success = true
 *             for (res in resources) {
 *                 if (res.lock.tryLock(300, TimeUnit.MILLISECONDS)) {
 *                     println("$threadName: захватил ${res.name}")
 *                     acquired.add(res)
 *                 } else {
 *                     println("$threadName: не удалось захватить ${res.name}")
 *                     success = false
 *                     break
 *                 }
 *             }
 *
 *             if (success) {
 *                 println("$threadName: успешно захватил все ресурсы")
 *                 Thread.sleep(500) // имитируем работу
 *                 break
 *             }
 *         } catch (e: InterruptedException) {
 *             println("$threadName: прерван")
 *             break
 *         } finally {
 *             acquired.forEach {
 *                 it.lock.unlock()
 *                 println("$threadName: освободил ${it.name}")
 *             }
 *         }
 *
 *         val waitTime = exponentialBackoffWithJitter(attempt)
 *         println("$threadName: ждёт $waitTime мс перед повтором")
 *         Thread.sleep(waitTime)
 *         attempt++
 *     }
 *
 *     if (attempt == maxAttempts) {
 *         println("$threadName: не удалось захватить все ресурсы после $maxAttempts попыток")
 *     }
 * }
 *
 * fun main() {
 *     val resourceA = Resource("ResourceA")
 *     val resourceB = Resource("ResourceB")
 *     val resourceC = Resource("ResourceC")
 *
 *     val thread1 = Thread {
 *         tryLockResources(listOf(resourceA, resourceB), "Поток 1")
 *     }
 *
 *     val thread2 = Thread {
 *         tryLockResources(listOf(resourceB, resourceC), "Поток 2")
 *     }
 *
 *     val thread3 = Thread {
 *         tryLockResources(listOf(resourceA, resourceC), "Поток 3")
 *     }
 *
 *     thread1.start()
 *     thread2.start()
 *     thread3.start()
 * }
 * ```
 *
 * ---
 *
 * ### 🔍 Что делает этот код:
 * - Каждый поток пытается захватить **набор ресурсов**.
 * - Если не удалось — освобождает уже захваченные и ждёт с **экспоненциальной задержкой и случайным разбросом**.
 * - Это снижает вероятность взаимной блокировки и делает поведение системы более устойчивым.
 *
 */
fun tryLockResources(resources: List<Resource>, threadName: String, maxAttempts: Int = 5) {
    var attempt = 0
    while (attempt < maxAttempts) {
        println("$threadName: попытка $attempt захватить ресурсы ${resources.map { it.name }}")

        val acquired = mutableListOf<Resource>()
        try {
            var success = true
            for (res in resources) {
                if (res.lock.tryLock(300, TimeUnit.MILLISECONDS)) {
                    println("$threadName: захватил ${res.name}")
                    acquired.add(res)
                } else {
                    println("$threadName: не удалось захватить ${res.name}")
                    success = false
                    break
                }
            }

            if (success) {
                println("$threadName: успешно захватил все ресурсы")
                Thread.sleep(500) // имитируем работу
                break
            }
        } catch (e: InterruptedException) {
            println("$threadName: прерван")
            break
        } finally {
            acquired.forEach {
                it.lock.unlock()
                println("$threadName: освободил ${it.name}")
            }
        }

        val waitTime = exponentialBackoffWithJitter(attempt)
        println("$threadName: ждёт $waitTime мс перед повтором")
        Thread.sleep(waitTime)
        attempt++
    }

    if (attempt == maxAttempts) {
        println("$threadName: не удалось захватить все ресурсы после $maxAttempts попыток")
    }
}

fun main() {
    val resourceA = Resource("ResourceA")
    val resourceB = Resource("ResourceB")
    val resourceC = Resource("ResourceC")

    val thread1 = Thread {
        tryLockResources(listOf(resourceA, resourceB), "Поток 1")
    }

    val thread2 = Thread {
        tryLockResources(listOf(resourceB, resourceC), "Поток 2")
    }

    val thread3 = Thread {
        tryLockResources(listOf(resourceA, resourceC), "Поток 3")
    }

    thread1.start()
    thread2.start()
    thread3.start()
}
