package tv.codealong.tutorials.various.locks

import java.lang.management.ManagementFactory
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

/**
 * Программный мониторинг из тестов
 * Версия с флагом остановки
 *
 * ```
 * Exception in thread "FJP-Monitor" java.lang.InterruptedException: sleep interrupted
 * 	at java.base/java.lang.Thread.sleep0(Native Method)
 * 	at java.base/java.lang.Thread.sleep(Thread.java:509)
 * 	at tv.codealong.tutorials.various.locks.ForkJoinPoolMonitor$Companion$startMonitoring$1.invoke(ForkJoinPoolMonitor.kt:30)
 * 	at tv.codealong.tutorials.various.locks.ForkJoinPoolMonitor$Companion$startMonitoring$1.invoke(ForkJoinPoolMonitor.kt:27)
 * 	at kotlin.concurrent.ThreadsKt$thread$thread$1.run(Thread.kt:30)
 * ```
 * 🐛 Причина исключения:
 * Когда вы вызываете stopMonitoring(), поток мониторинга прерывается (interrupt()), и если в этот момент поток спал в
 * Thread.sleep(), метод бросает InterruptedException.
 *
 * ✅ Это нормально и ожидаемо!
 * Исключение InterruptedException - это механизм Java для корректного завершения спящих потоков.
 *
 */
class ForkJoinPoolMonitor {

    companion object {
        private val isRunning = AtomicBoolean(false)
        private val lock = ReentrantLock()
        private var monitoringThread: Thread? = null

        /**
         * 🎯 Почему InterruptedException это правильно:
         * - InterruptedException - это не ошибка, а механизм для кооперативной отмены потоков
         * - Поток должен реагировать на прерывание - это best practice в Java
         * - Мы ловим исключение и корректно завершаемся
         * <br>
         * Best practices для работы с InterruptedException:
         * - Не игнорируйте его - всегда обрабатывайте
         * - Восстанавливайте interrupted status (если не можете обработать):
         *
         * ```
         * } catch (e: InterruptedException) {
         *     Thread.currentThread().interrupt() // Восстанавливаем статус
         *     break
         * }
         * ```
         * - Завершайте работу при получении прерывания
         * <br>
         *  ⚠️ Когда нужно ВОССТАНАВЛИВАТЬ interrupted status:
         * Восстановление статуса нужно, если вы не можете сразу завершить работу и должны передать сигнал прерывания дальше
         *  СЛУЧАЙ 1: Не можем сразу завершиться → передаем дальше
         *  Thread.currentThread().interrupt() // ← ВОССТАНАВЛИВАЕМ статус
         *  doSomeCleanupThatMightAlsoCheckInterruption()
         *
         *  СЛУЧАЙ 2: Работаем с блокирующими операциями
         *  Thread.currentThread().interrupt()
         *  throw RuntimeException("Operation cancelled", e)
         *
         *  ✅ В нашем случае это НЕ нужно, потому что:
         * - Вы сразу завершаете поток после прерывания
         * - Нет дальнейших блокирующих операций
         * - Поток просто заканчивает работу
         * <br>
         * ✅ В нашем случае это корректно, потому что:
         * - Мы выходим из цикла - исключение прерывает Thread.sleep(), поток продолжает выполнение
         * - Дальше идет finally блок - который гарантированно выполнится
         * - Поток завершается естественно - после окончания метода run()
         */
        @JvmStatic
        fun startMonitoring(intervalMs: Long = 3000) {
            if (isRunning.get()) return
            isRunning.set(true)

            monitoringThread = thread(name = "FJP-Monitor", isDaemon = true) {
                try {
                    while (isRunning.get()) {       // ✅ Проверяем флаг // 1. Проверка условия
                        monitorForkJoinPool()       // 2. Выполняем работу

                        Thread.sleep(intervalMs) // Каждые 3 секунды // 3. Спим → 🚨 INTERRUPTED!
                    }
                } catch (e: InterruptedException) {
                    // ✅ КОРРЕКТНАЯ ОБРАБОТКА: прерывание = сигнал к остановке
                    // ✅ КОРРЕКТНО: Мы ловим прерывание и просто завершаемся
                    // НЕ восстанавливаем статус, потому что поток завершается
                    // 4. Ловим исключение, выводим сообщение
                    println("Monitoring interrupted - stopping")
//                        Thread.currentThread().interrupt() // Восстанавливаем статус
//                        break // Выходим из цикла
                    //    Когда нужно Восстанавливаем статус?
                    //   // СЛУЧАЙ 1: Не можем сразу завершиться → передаем дальше
                    //    Thread.currentThread().interrupt() // ← ВОССТАНАВЛИВАЕМ статус
                    //    doSomeCleanupThatMightAlsoCheckInterruption()
                    //
                    //    // СЛУЧАЙ 2: Работаем с блокирующими операциями
                    //    Thread.currentThread().interrupt()
                    //    throw RuntimeException("Operation cancelled", e)
                } finally {
                    // 5. Гарантированно выполняем cleanup
                    println("ForkJoinPool monitoring stopped")
                }
                // 6. Поток завершается ← ✅ КОРРЕКТНО!
            }
        }


        @JvmStatic
        fun stopMonitoring() {
            isRunning.set(false)
            monitoringThread?.interrupt() // Посылаем сигнал прерывания
            monitoringThread = null
        }

        @JvmStatic
        fun monitorForkJoinPool() {
            lock.withLock {
                val threadBean = ManagementFactory.getThreadMXBean()
                val forkJoinPool = ForkJoinPool.commonPool()

                println("=== ForkJoinPool Monitor ===")
                println("Parallelism: ${forkJoinPool.parallelism}")
                println("Pool size: ${forkJoinPool.poolSize}")
                println("Active threads: ${forkJoinPool.activeThreadCount}")
                println("Queued tasks: ${forkJoinPool.queuedTaskCount}")
                println("Steal count: ${forkJoinPool.stealCount}")

                // Анализ состояния потоков
                threadBean.allThreadIds.asSequence()
                    .mapNotNull { threadBean.getThreadInfo(it) }
                    .filter { it.threadName.startsWith("ForkJoinPool") }
                    .forEach { info ->
                        println(
                            "Thread ${info.threadName}: " +
                                    "state=${info.threadState}, " +
                                    "blocked=${info.blockedTime}ms, " +
                                    "waited=${info.waitedTime}ms"
                        )
                    }
                println("============================")
            }
        }
    }
}

//Extension#ForkJoinPool monitoring started
//=== ForkJoinPool Monitor ===
//Parallelism: 11
//Pool size: 0
//Active threads: 0
//Queued tasks: 0
//Steal count: 0
//Test 1 started on thread: ForkJoinPool-1-worker-2
//Test 3 started on thread: ForkJoinPool-1-worker-4
//Test 2 started on thread: ForkJoinPool-1-worker-3
//Test 5 started on thread: ForkJoinPool-1-worker-1
//Thread ForkJoinPool-1-worker-1: state=TIMED_WAITING, blocked=-1ms, waited=-1ms
//Thread ForkJoinPool-1-worker-2: state=TIMED_WAITING, blocked=-1ms, waited=-1ms
//Thread ForkJoinPool-1-worker-3: state=TIMED_WAITING, blocked=-1ms, waited=-1ms
//Thread ForkJoinPool-1-worker-4: state=TIMED_WAITING, blocked=-1ms, waited=-1ms
//============================
//Test 4 started on thread: ForkJoinPool-1-worker-3
//Extension#ForkJoinPool monitoring stopped
//Monitoring interrupted - stopping
//ForkJoinPool monitoring stopped

//Extension#ForkJoinPool monitoring started
//=== ForkJoinPool Monitor ===
//Parallelism: 11
//Pool size: 0
//Active threads: 0
//Queued tasks: 0
//Steal count: 0
//Test 5 started on thread: ForkJoinPool-1-worker-1
//Test 2 started on thread: ForkJoinPool-1-worker-3
//Test 1 started on thread: ForkJoinPool-1-worker-2
//Test 3 started on thread: ForkJoinPool-1-worker-4
//Thread ForkJoinPool-1-worker-1: state=RUNNABLE, blocked=-1ms, waited=-1ms
//Thread ForkJoinPool-1-worker-2: state=RUNNABLE, blocked=-1ms, waited=-1ms
//Thread ForkJoinPool-1-worker-3: state=RUNNABLE, blocked=-1ms, waited=-1ms
//Thread ForkJoinPool-1-worker-4: state=RUNNABLE, blocked=-1ms, waited=-1ms
//============================
//Test 4 started on thread: ForkJoinPool-1-worker-3
//Extension#ForkJoinPool monitoring stopped
//Monitoring interrupted - stopping
//ForkJoinPool monitoring stopped