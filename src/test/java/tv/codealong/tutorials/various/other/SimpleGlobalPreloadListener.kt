package tv.codealong.tutorials.various.other

import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan

/**
 * Альтернативный упрощенный подход для глобальной предзагрузки для всех тестов.
 * Если не нужно рекурсивно обходить весь test plan
 */
class SimpleGlobalPreloadListener : TestExecutionListener {

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        println("🔍 Preloading test classes...")

        // Предзагрузка только по известным тестовым классам
        val testClasses = discoverTestClasses() // Ваш метод discovery

        testClasses.forEach { className ->
            safePreloadClass(className)
        }
    }

    private fun discoverTestClasses(): List<String> {
        // Здесь может быть ваша логика discovery
        // Например, через рефлексию или конфигурацию
        return listOf(
            "com.example.UserServiceTest",
            "com.example.ProductServiceTest",
            "com.example.OrderServiceTest"
        )
    }

    private fun safePreloadClass(className: String) {
        try {
            Class.forName(className)
            println("✅ Preloaded: $className")
        } catch (e: Exception) {
            println("⚠️  Failed to preload: $className")
        }
    }
}