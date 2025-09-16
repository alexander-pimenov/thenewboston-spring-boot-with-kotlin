package tv.codealong.tutorials.various.multithreading.issuese

import java.lang.management.ManagementFactory

/**
 *  2. Для голодания (Starvation)
 * Признаки голодания:
 *
 * Высокий CPU но низкая производительность
 *
 * Неравномерное время отклика
 *
 * Некоторые потоки никогда не завершаются
 *
 * Инструменты диагностики:
 *
 * - JMX-мониторинг
 */
fun monitorThreadStarvation() {
    val threadBean = ManagementFactory.getThreadMXBean()
    val allThreads = threadBean.dumpAllThreads(false, false)

    allThreads.forEach { threadInfo ->
        val blockedTime = threadInfo.blockedTime
        val waitedTime = threadInfo.waitedTime

        if (blockedTime > 10000 || waitedTime > 30000) { // 10 sec blocked, 30 sec waited
            println("ПОТЕНЦИАЛЬНОЕ ГОЛОДАНИЕ: ${threadInfo.threadName} " +
                    "blocked: ${blockedTime}ms, waited: ${waitedTime}ms")
        }
    }
}

fun main() {
    monitorThreadStarvation()
}