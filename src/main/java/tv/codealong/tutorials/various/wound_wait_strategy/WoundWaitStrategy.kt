package tv.codealong.tutorials.various.wound_wait_strategy

import java.util.concurrent.locks.ReentrantLock

data class Transaction(val id: Int, val priority: Int)

class Resource(val name: String, val lock: ReentrantLock = ReentrantLock(), var owner: Transaction? = null)

fun woundWait(requester: Transaction, resource: Resource): Boolean {
    synchronized(resource) {
        val currentOwner = resource.owner
        if (currentOwner == null || currentOwner == requester) {
            resource.lock.lock()
            resource.owner = requester
            println("Transaction ${requester.id} acquired ${resource.name}")
            return true
        }

        return if (requester.priority < currentOwner.priority) {
            // Wound: requester has higher priority
            println("Transaction ${requester.id} wounds ${currentOwner.id} for ${resource.name}")
            resource.lock.unlock()
            resource.owner = requester
            resource.lock.lock()
            println("Transaction ${requester.id} forcibly acquired ${resource.name}")
            true
        } else {
            // Wait: requester has lower priority
            println("Transaction ${requester.id} waits for ${resource.name}")
            false
        }
    }
}

/**
 * Давай я покажу тебе реализацию одной из самых интересных стратегий — **алгоритма Wound-Wait**,
 * который используется в базах данных для предотвращения взаимной блокировки.
 *
 * ---
 *
 * ### ⚔️ Алгоритм Wound-Wait — приоритетный захват
 *
 * **Суть стратегии:**
 * - Потоки получают приоритеты (например, по времени запуска).
 * - Если поток с **высоким приоритетом** хочет ресурс, занятый потоком с **низким приоритетом**, он «ранит» его — то есть отбирает ресурс.
 * - Если наоборот — поток с низким приоритетом просто «ждёт».
 *
 * ---
 *
 * ### 🧪 Пример на Kotlin
 *
 * ```kotlin
 * import java.util.concurrent.locks.ReentrantLock
 * import java.util.concurrent.TimeUnit
 *
 * data class Transaction(val id: Int, val priority: Int)
 *
 * class Resource(val name: String, val lock: ReentrantLock = ReentrantLock(), var owner: Transaction? = null)
 *
 * fun woundWait(requester: Transaction, resource: Resource): Boolean {
 *     synchronized(resource) {
 *         val currentOwner = resource.owner
 *         if (currentOwner == null || currentOwner == requester) {
 *             resource.lock.lock()
 *             resource.owner = requester
 *             println("Transaction ${requester.id} acquired ${resource.name}")
 *             return true
 *         }
 *
 *         return if (requester.priority < currentOwner.priority) {
 *             // Wound: requester has higher priority
 *             println("Transaction ${requester.id} wounds ${currentOwner.id} for ${resource.name}")
 *             resource.lock.unlock()
 *             resource.owner = requester
 *             resource.lock.lock()
 *             println("Transaction ${requester.id} forcibly acquired ${resource.name}")
 *             true
 *         } else {
 *             // Wait: requester has lower priority
 *             println("Transaction ${requester.id} waits for ${resource.name}")
 *             false
 *         }
 *     }
 * }
 *
 * fun main() {
 *     val resourceA = Resource("ResourceA")
 *     val tx1 = Transaction(id = 1, priority = 1) // более высокий приоритет
 *     val tx2 = Transaction(id = 2, priority = 2)
 *
 *     val thread1 = Thread {
 *         if (woundWait(tx1, resourceA)) {
 *             Thread.sleep(500)
 *             resourceA.lock.unlock()
 *             println("Transaction ${tx1.id} released ${resourceA.name}")
 *         }
 *     }
 *
 *     val thread2 = Thread {
 *         Thread.sleep(100) // запускается чуть позже
 *         if (woundWait(tx2, resourceA)) {
 *             Thread.sleep(500)
 *             resourceA.lock.unlock()
 *             println("Transaction ${tx2.id} released ${resourceA.name}")
 *         }
 *     }
 *
 *     thread1.start()
 *     thread2.start()
 * }
 * ```
 *
 * ---
 *
 * ### 🔍 Что происходит:
 * - `tx1` имеет более высокий приоритет, чем `tx2`.
 * - Если `tx2` попытается захватить ресурс, занятый `tx1`, он будет **ждать**.
 * - Если наоборот — `tx1` может **отобрать** ресурс у `tx2`.
 *
 * ---
 *
 */
fun main() {
    val resourceA = Resource("ResourceA")
    val tx1 = Transaction(id = 1, priority = 1) // более высокий приоритет
    val tx2 = Transaction(id = 2, priority = 2)

    val thread1 = Thread {
        if (woundWait(tx1, resourceA)) {
            Thread.sleep(500)
            resourceA.lock.unlock()
            println("Transaction ${tx1.id} released ${resourceA.name}")
        }
    }

    val thread2 = Thread {
        Thread.sleep(100) // запускается чуть позже
        if (woundWait(tx2, resourceA)) {
            Thread.sleep(500)
            resourceA.lock.unlock()
            println("Transaction ${tx2.id} released ${resourceA.name}")
        }
    }

    thread1.start()
    thread2.start()
}
