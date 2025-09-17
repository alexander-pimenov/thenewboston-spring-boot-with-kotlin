package tv.codealong.tutorials.various.other

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import tv.codealong.tutorials.various.locks.ForkJoinPoolMonitor

/**
 * 1. Что лучше: Extension или Object?
 * Extension - лучше!
 * Почему: Он гарантирует однократное выполнение на весь test run.
 */
class ForkJoinPoolMonitoringExtension : BeforeAllCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext) {
        ForkJoinPoolMonitor.startMonitoring()
        println("Extension#ForkJoinPool monitoring started")
    }

    override fun afterAll(context: ExtensionContext) {
        ForkJoinPoolMonitor.stopMonitoring()
        println("Extension#ForkJoinPool monitoring stopped")
    }
}

