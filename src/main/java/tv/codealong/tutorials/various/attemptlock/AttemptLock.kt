package tv.codealong.tutorials.various.attemptlock

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit

class Resource(val name: String, val lock: ReentrantLock = ReentrantLock())

fun main() {
    val resourceA = Resource("ResourceA")
    val resourceB = Resource("ResourceB")

    val thread1 = Thread {
        attemptLock(resourceA, resourceB, "Thread 1")
    }

    val thread2 = Thread {
        attemptLock(resourceB, resourceA, "Thread 2")
    }

    thread1.start()
    thread2.start()
}

/**
 * 🧠 Как это работает:
 * - Поток пытается захватить первый ресурс с таймаутом.
 * - Если удалось — пытается захватить второй.
 * - Если второй не удалось — освобождает первый и выходит, избегая deadlock.
 * - tryLock() с таймаутом позволяет избежать бесконечного ожидания.
 *
 * Такой подход особенно полезен в многопоточных системах, где ресурсы могут быть заняты непредсказуемо.
 */
fun attemptLock(first: Resource, second: Resource, threadName: String) {
    try {
        if (first.lock.tryLock(500, TimeUnit.MILLISECONDS)) {
            println("$threadName: locked ${first.name}")
            try {
                Thread.sleep(100) // имитируем работу
                if (second.lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                    try {
                        println("$threadName: locked ${second.name}")
                        // работа с обоими ресурсами
                    } finally {
                        second.lock.unlock()
                        println("$threadName: released ${second.name}")
                    }
                } else {
                    println("$threadName: could not lock ${second.name}, releasing ${first.name}")
                }
            } finally {
                first.lock.unlock()
                println("$threadName: released ${first.name}")
            }
        } else {
            println("$threadName: could not lock ${first.name}")
        }
    } catch (e: InterruptedException) {
        println("$threadName: interrupted")
    }
}
