package tv.codealong.tutorials.various.locks

import java.lang.management.ManagementFactory
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

/**
 * Программный мониторинг из тестов
 */
class ForkJoinPoolMonitor {

    companion object {
        private val isRunning = AtomicBoolean(false)
        private val lock = ReentrantLock()
        private val monitoringThread by lazy { startMonitoring() }

        @JvmStatic
        fun startMonitoring() = thread(name = "FJP-Monitor", isDaemon = true) {
            while (true) {
                monitorForkJoinPool()
                Thread.sleep(3000) // Каждые 3 секунды
            }
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
                val threadIds = threadBean.allThreadIds
                threadBean.getThreadInfo(threadIds).forEach { info ->
                    if (info?.threadName?.startsWith("ForkJoinPool") == true) {
                        println("Thread ${info.threadName}: " +
                                "state=${info.threadState}, " +
                                "blocked=${info.blockedTime}ms, " +
                                "waited=${info.waitedTime}ms")
                    }
                }
                println("============================")
            }
        }
    }
}