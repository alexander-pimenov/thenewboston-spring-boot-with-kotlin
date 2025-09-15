package tv.codealong.tutorials.various.locks

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

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