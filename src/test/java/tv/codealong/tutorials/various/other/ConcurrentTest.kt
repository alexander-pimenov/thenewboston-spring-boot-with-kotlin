package tv.codealong.tutorials.various.other

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import tv.codealong.tutorials.various.locks.ForkJoinPoolMonitor
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import kotlin.test.fail

/**
 * Использование в тестах
 * Настройка мониторинга в блоке @ExtendWith, в котором используется класс
 * ForkJoinPoolMonitoringExtension с переопределенными методами интерфейсов
 * BeforeAllCallback, AfterAllCallback
 *
 */
@ExtendWith(TestClassPreloadingExtension::class) // этот для примера глобальной загрузки многих классов
// - Более практичный подход - Часто проще использовать @BeforeAll в базовом классе
@ExtendWith(ClassPreloadingExtensionWithAtomic::class)
@ExtendWith(ForkJoinPoolMonitoringExtension::class)
@Execution(ExecutionMode.CONCURRENT)
class ConcurrentTest {


    @Test
    fun test1() {
        println("Test 1 started on thread: ${Thread.currentThread().name}")
        TimeUnit.MILLISECONDS.sleep(500)
    }

    @Test
    fun test2() {
        println("Test 2 started on thread: ${Thread.currentThread().name}")
        TimeUnit.MILLISECONDS.sleep(300)
    }

    @Test
    fun test3() {
        println("Test 3 started on thread: ${Thread.currentThread().name}")
        TimeUnit.MILLISECONDS.sleep(700)
    }

    @Test
    fun test4() {
        println("Test 4 started on thread: ${Thread.currentThread().name}")
        TimeUnit.MILLISECONDS.sleep(200)
    }


    @Test
    fun testWithDeadlockDetection() {
        println("Test 5 started on thread: ${Thread.currentThread().name}")
        val threadBean = ManagementFactory.getThreadMXBean()

        // Проверка на дедлоки перед тестом
        val deadlockedThreads = threadBean.findDeadlockedThreads()
        if (deadlockedThreads != null) {
            fail("Deadlock detected before test execution!")
        }

        // Ваш тест...
        TimeUnit.MILLISECONDS.sleep(2000)

        // Проверка после теста
        Thread.sleep(1000) // Даем время для проявления проблем
        val newDeadlocks = threadBean.findDeadlockedThreads()
        if (newDeadlocks != null) {
            println("Deadlock detected after test!")
            threadBean.getThreadInfo(newDeadlocks).forEach { info ->
                println("Deadlocked thread: ${info?.threadName}")
            }
        }
    }
}