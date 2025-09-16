package tv.codealong.tutorials.various.locks

import tv.codealong.tutorials.various.locks.ForkJoinPoolMonitor.Companion.monitorForkJoinPool
import kotlin.concurrent.thread

/**
 * Версия с автоматической остановкой по таймауту
 */
object SafeForkJoinPoolMonitor {
    private var monitoringThread: Thread? = null

    @JvmStatic
    fun startMonitoring(durationMinutes: Int = 5) {
        stopMonitoring() // Останавливаем предыдущий, если был

        monitoringThread = thread(name = "FJP-Monitor", isDaemon = true) {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + durationMinutes * 60 * 1000

            while (System.currentTimeMillis() < endTime) { // ✅ По таймауту
                monitorForkJoinPool()
                try {
                    Thread.sleep(3000)
                } catch (e: InterruptedException) {
                    println("Monitoring interrupted")
                    break
                } finally {
                    println("ForkJoinPool monitoring completed")
                }
            }
        }
    }

    @JvmStatic
    fun stopMonitoring() {
        monitoringThread?.interrupt() // Посылаем сигнал прерывания
        monitoringThread = null
    }
}