package tv.codealong.tutorials.various.exponential_backoff_and_jitter_and_executorService2

import java.io.File
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.min
import kotlin.random.Random

data class Resource(val name: String, val lock: ReentrantLock = ReentrantLock())
data class Task(val id: Int, val priority: Int, val resources: List<Resource>)

val logFile = File("task_log.txt")
val successCounter = AtomicInteger(0)

fun log(message: String) {
    println(message)
    logFile.appendText("$message\n")
}

fun exponentialBackoffWithJitter(attempt: Int): Long {
    val baseDelay = 100L
    val maxDelay = 2000L
    val expDelay = min(baseDelay * (1 shl attempt), maxDelay)
    return Random.nextLong(expDelay / 2, expDelay)
}

/**
 * Давай добавим в наш пул задач:
 *
 * 1. **Логирование в файл** — чтобы отслеживать поведение задач.
 * 2. **Метрики успешности** — сколько задач успешно захватили ресурсы.
 * 3. **Приоритеты задач** — чтобы старшие задачи обрабатывались раньше.
 *
 * ---
 *
 * ### 🧪 Расширенный пример: пул задач с логированием, метриками и приоритетами
 *
 * ```kotlin
 * import java.io.File
 * import java.util.concurrent.*
 * import java.util.concurrent.locks.ReentrantLock
 * import kotlin.math.min
 * import kotlin.random.Random
 *
 * data class Resource(val name: String, val lock: ReentrantLock = ReentrantLock())
 * data class Task(val id: Int, val priority: Int, val resources: List<Resource>)
 *
 * val logFile = File("task_log.txt")
 * val successCounter = AtomicInteger(0)
 *
 * fun log(message: String) {
 *     println(message)
 *     logFile.appendText("$message\n")
 * }
 *
 * fun exponentialBackoffWithJitter(attempt: Int): Long {
 *     val baseDelay = 100L
 *     val maxDelay = 2000L
 *     val expDelay = min(baseDelay * (1 shl attempt), maxDelay)
 *     return Random.nextLong(expDelay / 2, expDelay)
 * }
 *
 * fun createTask(task: Task, maxAttempts: Int = 5): Runnable {
 *     return Runnable {
 *         var attempt = 0
 *         val taskName = "Задача ${task.id} (приоритет ${task.priority})"
 *         while (attempt < maxAttempts) {
 *             log("$taskName: попытка $attempt захватить ресурсы ${task.resources.map { it.name }}")
 *
 *             val acquired = mutableListOf<Resource>()
 *             try {
 *                 var success = true
 *                 for (res in task.resources) {
 *                     if (res.lock.tryLock(300, TimeUnit.MILLISECONDS)) {
 *                         log("$taskName: захватил ${res.name}")
 *                         acquired.add(res)
 *                     } else {
 *                         log("$taskName: не удалось захватить ${res.name}")
 *                         success = false
 *                         break
 *                     }
 *                 }
 *
 *                 if (success) {
 *                     log("$taskName: успешно захватил все ресурсы")
 *                     Thread.sleep(500)
 *                     successCounter.incrementAndGet()
 *                     break
 *                 }
 *             } catch (e: InterruptedException) {
 *                 log("$taskName: прерван")
 *                 break
 *             } finally {
 *                 acquired.forEach {
 *                     it.lock.unlock()
 *                     log("$taskName: освободил ${it.name}")
 *                 }
 *             }
 *
 *             val waitTime = exponentialBackoffWithJitter(attempt)
 *             log("$taskName: ждёт $waitTime мс перед повтором")
 *             Thread.sleep(waitTime)
 *             attempt++
 *         }
 *
 *         if (attempt == maxAttempts) {
 *             log("$taskName: не удалось захватить ресурсы после $maxAttempts попыток")
 *         }
 *     }
 * }
 *
 * fun main() {
 *     val resourceA = Resource("ResourceA")
 *     val resourceB = Resource("ResourceB")
 *     val resourceC = Resource("ResourceC")
 *
 *     val tasks = listOf(
 *         Task(1, priority = 2, resources = listOf(resourceA, resourceB)),
 *         Task(2, priority = 1, resources = listOf(resourceB, resourceC)),
 *         Task(3, priority = 3, resources = listOf(resourceA, resourceC))
 *     )
 *
 *     // Сортируем задачи по приоритету (чем ниже число — тем выше приоритет)
 *     val sortedTasks = tasks.sortedBy { it.priority }
 *
 *     val executor = Executors.newFixedThreadPool(3)
 *     sortedTasks.forEach { executor.submit(createTask(it)) }
 *
 *     executor.shutdown()
 *     executor.awaitTermination(5, TimeUnit.SECONDS)
 *
 *     log("Всего успешных задач: ${successCounter.get()}")
 * }
 * ```
 *
 * ---
 *
 * ### 📌 Что добавлено:
 * - **Логирование** в файл `task_log.txt`.
 * - **Счётчик успешных задач** через `AtomicInteger`.
 * - **Приоритеты задач** — задачи с меньшим `priority` запускаются раньше.
 *
 */
fun createTask(task: Task, maxAttempts: Int = 5): Runnable {
    return Runnable {
        var attempt = 1
        val taskName = "Задача №_${task.id} (приоритет ${task.priority})"
        while (attempt < maxAttempts) {
            log("$taskName: попытка $attempt захватить ресурсы ${task.resources.map { it.name }}")

            val acquired = mutableListOf<Resource>()
            try {
                var success = true
                for (res in task.resources) {
                    if (res.lock.tryLock(300, TimeUnit.MILLISECONDS)) {
                        log("$taskName: захватил ${res.name}")
                        acquired.add(res)
                    } else {
                        log("$taskName: не удалось захватить ${res.name}")
                        success = false
                        break
                    }
                }

                if (success) {
                    log("$taskName: успешно захватил все ресурсы")
                    Thread.sleep(500)
                    successCounter.incrementAndGet()
                    break
                }
            } catch (e: InterruptedException) {
                log("$taskName: прерван")
                break
            } finally {
                acquired.forEach {
                    it.lock.unlock()
                    log("$taskName: освободил ${it.name}")
                }
            }

            val waitTime = exponentialBackoffWithJitter(attempt)
            log("$taskName: ждёт $waitTime мс перед повтором")
            Thread.sleep(waitTime)
            attempt++
        }

        if (attempt == maxAttempts) {
            log("$taskName: не удалось захватить ресурсы после $maxAttempts попыток")
        }
    }
}

fun main() {
    val resourceA = Resource("ResourceA")
    val resourceB = Resource("ResourceB")
    val resourceC = Resource("ResourceC")

    val tasks = listOf(
        Task(1, priority = 2, resources = listOf(resourceA, resourceB)),
        Task(2, priority = 1, resources = listOf(resourceB, resourceC)),
        Task(3, priority = 3, resources = listOf(resourceA, resourceC))
    )

    // Сортируем задачи по приоритету (чем ниже число — тем выше приоритет)
    val sortedTasks = tasks.sortedBy { it.priority }

    val executor = Executors.newFixedThreadPool(3)
    sortedTasks.forEach { executor.submit(createTask(it)) }

    executor.shutdown()
    executor.awaitTermination(5, TimeUnit.SECONDS)

    log("Всего успешных задач: ${successCounter.get()}")
}
