package tv.codealong.tutorials.various.other

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import tv.codealong.tutorials.various.locks.ForkJoinPoolMonitor
import java.lang.management.ManagementFactory
import java.util.concurrent.ForkJoinPool

class ThreadMonitoringListener : TestExecutionListener {

    private val forkJoinPool = ForkJoinPool.commonPool()


    override fun executionStarted(testIdentifier: TestIdentifier) {
        println("Test started: ${testIdentifier.displayName}")
        if (testIdentifier.isTest) {
            println("Test started: ${testIdentifier.displayName}")
            printPoolStatus("Test STARTED: ${testIdentifier.displayName}")
        }
        ForkJoinPoolMonitor.monitorForkJoinPool()
    }

    override fun executionFinished(
        testIdentifier: TestIdentifier,
        testExecutionResult: TestExecutionResult
    ) {
        if (testIdentifier.isTest) {
            println("Test finished: ${testIdentifier.displayName}")
            printPoolStatus("Test FINISHED: ${testIdentifier.displayName}")
            checkForDeadlocks()
        }
        ForkJoinPoolMonitor.monitorForkJoinPool()
    }

    private fun printPoolStatus(context: String) {
        println("\n=== $context ===")
        println("###ForkJoinPool status:###")
        println("  Parallelism: ${forkJoinPool.parallelism}")
        println("  Pool size: ${forkJoinPool.poolSize}")
        println("  Active threads: ${forkJoinPool.activeThreadCount}")
        println("  Queued tasks: ${forkJoinPool.queuedTaskCount}")
        println("  Steal count: ${forkJoinPool.stealCount}")
        println("======================")
    }

    private fun checkForDeadlocks() {
        val threadBean = ManagementFactory.getThreadMXBean()
        val deadlockedThreads = threadBean.findDeadlockedThreads()

        if (deadlockedThreads != null) {
            println("⚠️  DEADLOCK DETECTED!")
            threadBean.getThreadInfo(deadlockedThreads).forEach { info ->
                println("Deadlocked thread: ${info?.threadName}")
            }
        }
    }
}