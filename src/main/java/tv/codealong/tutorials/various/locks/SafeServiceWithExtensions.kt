package tv.codealong.tutorials.various.locks

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.random.Random
import kotlin.concurrent.thread

/**
 * Ключевые преимущества подхода с tryLock():
 * - Предотвращение "вечного" дедлока: Приложение не зависнет навсегда.
 * - Возможность логирования: Вы можете залогировать факт проблемы.
 * - Возможность восстановления: Можно предпринять альтернативные действия.
 * - Мониторинг: Легко отслеживать проблемы с блокировками.
 * - Более предсказуемое поведение: Таймауты делают систему более стабильной.
 *
 * Рекомендация: Используйте Вариант 2 с extension-функцией - он сочетает безопасность, читаемость и переиспользуемость
 * кода (Lock.tryWithLock). Для особо критичных мест Вариант 3 с повторными попытками.
 *
 * Ключевые особенности реализации:
 * Data класс - содержит основные поля и метод copyWithNewValue для неизменяемости
 *
 * processData() - имитирует операцию записи с обновлением версии
 *
 * readData() - имитирует операцию чтения
 *
 * Логирование операций - для отслеживания порядка выполнения
 *
 * Случайные задержки - имитируют реальную работу с данными
 *
 * Обработка ошибок - через Result тип для безопасного выполнения
 *
 * Многопоточный тест - демонстрирует работу в конкурентной среде
 *
 * Этот код можно сразу запускать и тестировать различные сценарии работы с lock'ами!
 */
@Service
class SafeServiceWithExtensions {
    private val lock by lazy { ReentrantReadWriteLock() }

    // Имитация хранилища данных
    private var storage: Data = Data(1, "initial_value", 1)
    private val log = mutableListOf<String>()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun processData(newValue: String): Result<Data> {
        return lock.writeLock().tryWithLock(5, TimeUnit.SECONDS) {
            // Имитация долгой операции
            Thread.sleep(Random.nextLong(100, 500))

            val newData: Data = storage.copyWithNewValue(newValue)
            storage = newData

            val message = "Processed: ${newData.value} (v${newData.version})"
            log.add(message)
            println("✓ $message")

            newData
        }
    }

    fun processData2(newValue: String): Data {
        return doWithWriteLock(lock) {
            // Имитация долгой операции
            Thread.sleep(Random.nextLong(100, 500))

            val newData: Data = storage.copyWithNewValue(newValue)
            storage = newData

            val message = "Processed: ${newData.value} (v${newData.version})"
            log.add(message)
            println("✓ $message")

            newData
        }
    }

    fun readData(): Result<Data> {
        return lock.readLock().tryWithLock(2, TimeUnit.SECONDS) {
            // Имитация чтения
            Thread.sleep(Random.nextLong(50, 200))

            val message = "Read: ${storage.value} (v${storage.version})"
            println("📖 $message")

            storage
        }
    }

    fun readData2(): Data {
        return doWithReadLock(lock) {
            // Имитация чтения
            Thread.sleep(Random.nextLong(50, 200))

            val message = "Read: ${storage.value} (v${storage.version})"
            println("📖 $message")

            storage
        }
    }

    fun readDataWithException(): Result<Data> {
        return lock.readLock().tryWithLock(500, TimeUnit.MILLISECONDS) {
            // Имитация проблем при чтении
            if (Random.nextBoolean()) {
                throw RuntimeException("Simulated read error!")
            }
            Thread.sleep(100)
            storage
        }
    }

    fun getOperationLog(): List<String> {
        return lock.readLock().tryWithLock(1, TimeUnit.SECONDS) {
            log.toList()
        }.getOrElse { emptyList() }
    }

    fun getOperationLog2(): List<String> {
        return doWithReadLock(lock) {
            log.toList()
        }
    }

    fun getCurrentData(): Data {
        return lock.readLock().tryWithLock(1, TimeUnit.SECONDS) {
            storage
        }.getOrElse { Data(-1, "error", -1) }
    }

    fun getCurrentData2(): Data {
        return doWithReadLock(lock) {
            storage
        }
    }

    fun clearLog() {
        lock.writeLock().tryWithLock(1, TimeUnit.SECONDS) {
            log.clear()
            println("Log cleared")
        }
    }

    fun clearLog2() {
        doWithWriteLock(lock) {
            log.clear()
            println("Log cleared")
        }
    }

    fun updateWithRetry(data: Data, maxAttempts: Int = 3): Boolean {
        var attempts = 0

        while (attempts < maxAttempts) {
            attempts++

            val lockAcquired: Boolean = lock.writeLock().tryLock(30, TimeUnit.SECONDS)
            if (lockAcquired) {
                return try {
                    processData(data.value)
                    true
                } finally {
                    lock.writeLock().unlock()
                }
            } else {
                logger.warn("Failed to acquire lock on attempt $attempts. Retrying...")
                Thread.sleep(1000) // Ждем перед повторной попыткой
            }
        }

        logger.error("All $maxAttempts attempts to acquire lock failed")
        return false
    }
}

// Простой тест в одном файле
//fun simpleTest() {
//    val service = SafeServiceWithExtensions()
//
//    println("Starting simple test...")
//
//    // Тест записи
//    val writeResult = service.processData("test_value")
//    if (writeResult.isSuccess) {
//        println("Write successful: ${writeResult.getOrNull()}")
//    } else {
//        println("Write failed: ${writeResult.exceptionOrNull()?.message}")
//    }
//
//    // Тест чтения
//    val readResult = service.readData()
//    if (readResult.isSuccess) {
//        println("Read successful: ${readResult.getOrNull()}")
//    } else {
//        println("Read failed: ${readResult.exceptionOrNull()?.message}")
//    }
//
//    // Тест с коротким таймаутом (должен упасть)
//    val timeoutResult = service.readLock.tryLock(10, TimeUnit.MILLISECONDS)
//    println("Quick timeout test: ${if (timeoutResult) "success" else "failed"}")
//    if (timeoutResult) service.readLock.unlock()
//}


// Тестовый запуск
fun main() {
    val dataService = SafeServiceWithExtensions()

    // Запускаем несколько потоков для тестирования
    val threads: List<Thread> = List(10) { threadId ->
        thread {
            repeat(5) { attempt ->
                try {
                    if (threadId % 3 == 0) {
                        // Потоки записи
                        val result: Result<Data> = dataService.processData("thread${threadId}_attempt$attempt")
                        if (result.isFailure) {
                            println("❌ Thread $threadId failed to write: ${result.exceptionOrNull()?.message}")
                        }
                    } else {
                        // Потоки чтения
                        val result: Result<Data> = dataService.readData()
                        if (result.isFailure) {
                            println("❌ Thread $threadId failed to read: ${result.exceptionOrNull()?.message}")
                        }
                    }
                } catch (e: Exception) {
                    println("💥 Thread $threadId crashed: ${e.message}")
                }
                Thread.sleep(Random.nextLong(100, 300))
            }
        }
    }

    // Ждем завершения всех потоков
    threads.forEach { it.join() }

    // Выводим финальный результат
    println("\n=== FINAL STATE ===")
    println("Current data: ${dataService.getCurrentData()}")
    println("Operation log: ${dataService.getOperationLog().takeLast(5)}")
}