package tv.codealong.tutorials.various.other

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import tv.codealong.tutorials.various.locks.ForkJoinPoolMonitor

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

