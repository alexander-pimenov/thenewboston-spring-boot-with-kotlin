package tv.codealong.tutorials.various.waitdie

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit

data class Transaction(val id: Int, val priority: Int)

class Resource(val name: String, val lock: ReentrantLock = ReentrantLock(), var owner: Transaction? = null)

fun waitDie(requester: Transaction, resource: Resource): Boolean {
    synchronized(resource) {
        val currentOwner = resource.owner
        if (currentOwner == null || currentOwner == requester) {
            resource.lock.lock()
            resource.owner = requester
            println("Transaction ${requester.id} acquired ${resource.name}")
            return true
        }

        return if (requester.priority < currentOwner.priority) {
            // Wait: requester has higher priority
            println("Transaction ${requester.id} waits for ${resource.name} (owned by ${currentOwner.id})")
            false // в реальной системе — ожидание или повторная попытка
        } else {
            // Die: requester has lower priority
            println("Transaction ${requester.id} dies trying to acquire ${resource.name} (owned by ${currentOwner.id})")
            false
        }
    }
}

/**
 * Давай теперь рассмотрим стратегию **Wait-Die**, которая также применяется в базах данных для предотвращения взаимной блокировки
 * — но действует немного иначе, чем Wound-Wait.
 *
 * ---
 *
 * ### 💀 Стратегия Wait-Die — «ждать или умереть»
 *
 * **Суть:**
 * - Потоки (или транзакции) получают приоритеты (например, по времени запуска).
 * - Если поток с **высоким приоритетом** хочет ресурс, занятый потоком с **низким приоритетом**, он **ждёт**.
 * - Если поток с **низким приоритетом** хочет ресурс, занятый более старшим — он **умирает** (откатывается или завершает попытку).
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
 * fun waitDie(requester: Transaction, resource: Resource): Boolean {
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
 *             // Wait: requester has higher priority
 *             println("Transaction ${requester.id} waits for ${resource.name} (owned by ${currentOwner.id})")
 *             false // в реальной системе — ожидание или повторная попытка
 *         } else {
 *             // Die: requester has lower priority
 *             println("Transaction ${requester.id} dies trying to acquire ${resource.name} (owned by ${currentOwner.id})")
 *             false
 *         }
 *     }
 * }
 *
 * fun main() {
 *     val resourceA = Resource("ResourceA")
 *     val tx1 = Transaction(id = 1, priority = 1) // старший
 *     val tx2 = Transaction(id = 2, priority = 2) // младший
 *
 *     val thread1 = Thread {
 *         if (waitDie(tx1, resourceA)) {
 *             Thread.sleep(500)
 *             resourceA.lock.unlock()
 *             println("Transaction ${tx1.id} released ${resourceA.name}")
 *         }
 *     }
 *
 *     val thread2 = Thread {
 *         Thread.sleep(100)
 *         if (waitDie(tx2, resourceA)) {
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
 * - `tx1` — старший, он может ждать, если ресурс занят.
 * - `tx2` — младший, если ресурс занят старшим, он «умирает» — не ждёт, а завершает попытку.
 *
 * ---
 *
 * ### 📌 Применение:
 * - Wait-Die снижает риск взаимной блокировки за счёт отказа младших потоков от ожидания.
 * - Часто используется в системах с высокой конкуренцией, где важна скорость отката.
 *
 */
fun main() {
    val resourceA = Resource("ResourceA")
    val tx1 = Transaction(id = 1, priority = 1) // старший
    val tx2 = Transaction(id = 2, priority = 2) // младший

    val thread1 = Thread {
        if (waitDie(tx1, resourceA)) {
            Thread.sleep(500)
            resourceA.lock.unlock()
            println("Transaction ${tx1.id} released ${resourceA.name}")
        }
    }

    val thread2 = Thread {
        Thread.sleep(100)
        if (waitDie(tx2, resourceA)) {
            Thread.sleep(500)
            resourceA.lock.unlock()
            println("Transaction ${tx2.id} released ${resourceA.name}")
        }
    }

    thread1.start()
    thread2.start()
}


