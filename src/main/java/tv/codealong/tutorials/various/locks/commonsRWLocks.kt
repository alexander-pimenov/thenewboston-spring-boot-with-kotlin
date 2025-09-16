package tv.codealong.tutorials.various.locks

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

inline fun <T> withLock(
    rwLock: ReentrantReadWriteLock,
    mode: LockMode,
    lambda: () -> T
): T {
    val lock = when (mode) {
        LockMode.READ -> rwLock.readLock()
        LockMode.WRITE -> rwLock.writeLock()
    }
    if (lock.tryLock(5, TimeUnit.MINUTES)) {
        try {
            return lambda.invoke()
        } catch (e: InterruptedException) {
            throw RuntimeException("Не удалось получить блокировку на ${mode.desc} из-за прерывания потока.")
        } finally {
            lock.unlock()
        }
    } else {
        throw RuntimeException("Не удалось захватить блокировку на ${mode.desc} в течение 5 минут.")
    }
}

fun <T> withLock(
    rwLock: ReentrantReadWriteLock,
    mode: LockMode,
    lambda: LambdaForLock
): T {
    val lock = when (mode) {
        LockMode.READ -> rwLock.readLock()
        LockMode.WRITE -> rwLock.writeLock()
    }
    if (lock.tryLock(5, TimeUnit.MINUTES)) {
        try {
            return lambda.invoke()
        } catch (e: InterruptedException) {
            throw RuntimeException("Не удалось получить блокировку на ${mode.desc} из-за прерывания потока.")
        } finally {
            lock.unlock()
        }
    } else {
        throw RuntimeException("Не удалось захватить блокировку на ${mode.desc} в течение 5 минут.")
    }
}

enum class LockMode(val desc: String) {
    READ("чтение"),
    WRITE("запись")
}

fun <T> doWithReadLock(wrLock: ReentrantReadWriteLock, lambda: () -> T): T {
    return withLock(wrLock, LockMode.READ, lambda)
}

fun <T> doWithWriteLock(wrLock: ReentrantReadWriteLock, lambda: () -> T): T {
    return withLock(wrLock, LockMode.WRITE, lambda)
}

fun <T> doWithReadLock(wrLock: ReentrantReadWriteLock, lambda: LambdaForLock): T {
    return withLock(wrLock, LockMode.READ, lambda)
}

fun <T> doWithWriteLock(wrLock: ReentrantReadWriteLock, lambda: LambdaForLock): T {
    return withLock(wrLock, LockMode.WRITE, lambda)
}

interface LambdaForLock {
    fun <T> invoke(): T
}


// Расширение для удобной работы с lock'ами
fun <T> Lock.tryWithLock(
    time: Long,
    unit: TimeUnit,
    action: () -> T
): Result<T> {
    return if (tryLock(time, unit)) {
        try {
            Result.success(action())
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            unlock()
        }
    } else {
        Result.failure(IllegalStateException("Failed to acquire lock in ${unit.toSeconds(time)} seconds"))
    }
}

/**
 * ReentrantLock(true) - Справедливая блокировка
 * Как работает:
 * FIFO очередь - потоки получают доступ в порядке поступления
 * Предотвращает голодание - каждый поток рано или поздно получит доступ
 * Низкая производительность - больше накладных расходов
 */
fun testFairLock() {
    val lock = ReentrantLock(true) // Справедливая

    repeat(15) { i ->
        thread {
            lock.lock()
            try {
                println("Поток $i получил lock (${Thread.currentThread().name})")
                Thread.sleep(100)
            } finally {
                lock.unlock()
            }
        }
    }
}
// Вывод будет близок к: 0, 1, 2, 3, 4 - порядок сохраняется
//Поток 0 получил lock
//Поток 7 получил lock
//Поток 5 получил lock
//Поток 1 получил lock
//Поток 3 получил lock
//Поток 4 получил lock
//Поток 8 получил lock
//Поток 6 получил lock
//Поток 10 получил lock
//Поток 9 получил lock
//Поток 2 получил lock
//Поток 11 получил lock
//Поток 13 получил lock
//Поток 12 получил lock
//Поток 14 получил lock

/**
 * ReentrantLock() - Несправедливая блокировка (по умолчанию)
 * Как работает:
 * Порядок не гарантируется - поток может "перепрыгнуть" очередь
 * Высокая производительность - меньше переключений контекста
 * Риск голодания - некоторые потоки могут ждать очень долго
 */
fun testUnfairLock() {
    val lock = ReentrantLock() // Несправедливая

    repeat(15) { i ->
        thread {
            lock.lock()
            try {
                println("Поток $i получил lock (${Thread.currentThread().name})")
                Thread.sleep(100)
            } finally {
                lock.unlock()
            }
        }
    }
}
// Возможный вывод: 0, 0, 0, 1, 2 - первый поток монополизирует доступ
//Поток 0 получил lock
//Поток 2 получил lock
//Поток 1 получил lock
//Поток 3 получил lock
//Поток 4 получил lock
//Поток 5 получил lock
//Поток 6 получил lock
//Поток 7 получил lock
//Поток 8 получил lock
//Поток 10 получил lock
//Поток 11 получил lock
//Поток 12 получил lock
//Поток 9 получил lock
//Поток 13 получил lock
//Поток 14 получил lock

fun main() {
    testFairLock()
    println("\n\n")
    testUnfairLock()
    println("\n\n")
    performanceTest()
}

//Тест производительности
fun performanceTest() {
    val unfairLock = ReentrantLock()
    val fairLock = ReentrantLock(true)

    val timeUnfair = measureTimeMillis {
        repeat(100000) {
            unfairLock.lock()
            unfairLock.unlock()
        }
    }

    val timeFair = measureTimeMillis {
        repeat(100000) {
            fairLock.lock()
            fairLock.unlock()
        }
    }

    println("Несправедливая: $timeUnfair ms")
    println("Справедливая: $timeFair ms")
    println("Разница: ${(timeFair - timeUnfair)} ms")
}