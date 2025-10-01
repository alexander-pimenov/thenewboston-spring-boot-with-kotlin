package tv.codealong.tutorials.various.exponentialbackoffandjitterandexecutorservice


import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
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
 * Адаптация стратегии **Exponential Backoff с Jitter** под **пул задач** — это мощный способ сделать многопоточную
 * систему устойчивой, особенно когда задачи конкурируют за ресурсы. Давай я покажу, как это можно реализовать
 * с использованием `ExecutorService` — стандартного пула потоков в Kotlin/Java.
 *
 * ---
 *
 * ### 🧪 Пример: пул задач + Backoff + Jitter
 *
 * ```kotlin
 * import java.util.concurrent.*
 * import java.util.concurrent.locks.ReentrantLock
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
 * fun createTask(id: Int, resources: List<Resource>, maxAttempts: Int = 5): Runnable {
 *     return Runnable {
 *         var attempt = 0
 *         val taskName = "Задача $id"
 *         while (attempt < maxAttempts) {
 *             println("$taskName: попытка $attempt захватить ресурсы ${resources.map { it.name }}")
 *
 *             val acquired = mutableListOf<Resource>()
 *             try {
 *                 var success = true
 *                 for (res in resources) {
 *                     if (res.lock.tryLock(300, TimeUnit.MILLISECONDS)) {
 *                         println("$taskName: захватил ${res.name}")
 *                         acquired.add(res)
 *                     } else {
 *                         println("$taskName: не удалось захватить ${res.name}")
 *                         success = false
 *                         break
 *                     }
 *                 }
 *
 *                 if (success) {
 *                     println("$taskName: успешно захватил все ресурсы")
 *                     Thread.sleep(500) // имитация работы
 *                     break
 *                 }
 *             } catch (e: InterruptedException) {
 *                 println("$taskName: прерван")
 *                 break
 *             } finally {
 *                 acquired.forEach {
 *                     it.lock.unlock()
 *                     println("$taskName: освободил ${it.name}")
 *                 }
 *             }
 *
 *             val waitTime = exponentialBackoffWithJitter(attempt)
 *             println("$taskName: ждёт $waitTime мс перед повтором")
 *             Thread.sleep(waitTime)
 *             attempt++
 *         }
 *
 *         if (attempt == maxAttempts) {
 *             println("$taskName: не удалось захватить ресурсы после $maxAttempts попыток")
 *         }
 *     }
 * }
 *
 * fun main() {
 *     val resourceA = Resource("ResourceA")
 *     val resourceB = Resource("ResourceB")
 *     val resourceC = Resource("ResourceC")
 *
 *     val executor = Executors.newFixedThreadPool(3)
 *
 *     executor.submit(createTask(1, listOf(resourceA, resourceB)))
 *     executor.submit(createTask(2, listOf(resourceB, resourceC)))
 *     executor.submit(createTask(3, listOf(resourceA, resourceC)))
 *
 *     executor.shutdown()
 * }
 * ```
 *
 * ---
 *
 * ### 🔍 Что делает этот код:
 * - Создаёт пул из 3 потоков (`FixedThreadPool`).
 * - Каждая задача — это поток, который пытается захватить набор ресурсов.
 * - Если не удалось — использует **экспоненциальную задержку с Jitter** и повторяет попытку.
 * - Все задачи управляются централизованно через `ExecutorService`.
 *
 * ---
 *
 */
fun createTask(id: Int, resources: List<Resource>, maxAttempts: Int = 5): Runnable {
    return Runnable {
        var attempt = 1
        val taskName = "Задача №_$id"
        while (attempt < maxAttempts) {
            println("$taskName: попытка $attempt захватить ресурсы ${resources.map { it.name }}")

            val acquired = mutableListOf<Resource>()
            try {
                var success = true
                for (res in resources) {
                    if (res.lock.tryLock(300, TimeUnit.MILLISECONDS)) {
                        println("$taskName: захватил ${res.name}")
                        acquired.add(res)
                    } else {
                        println("$taskName: не удалось захватить ${res.name}")
                        success = false
                        break
                    }
                }

                if (success) {
                    println("$taskName: успешно захватил все ресурсы")
                    Thread.sleep(500) // имитация работы
                    break
                }
            } catch (e: InterruptedException) {
                println("$taskName: прерван")
                break
            } finally {
                acquired.forEach {
                    it.lock.unlock()
                    println("$taskName: освободил ${it.name}")
                }
            }

            val waitTime = exponentialBackoffWithJitter(attempt)
            println("$taskName: ждёт $waitTime мс перед повтором")
            Thread.sleep(waitTime)
            attempt++
        }

        if (attempt == maxAttempts) {
            println("$taskName: не удалось захватить ресурсы после $maxAttempts попыток")
        }
    }
}

fun main() {
    val resourceA = Resource("ResourceA")
    val resourceB = Resource("ResourceB")
    val resourceC = Resource("ResourceC")

    val executor = Executors.newFixedThreadPool(3)

    executor.submit(createTask(1, listOf(resourceA, resourceB)))
    executor.submit(createTask(2, listOf(resourceB, resourceC)))
    executor.submit(createTask(3, listOf(resourceA, resourceC)))

    executor.shutdown()
}
