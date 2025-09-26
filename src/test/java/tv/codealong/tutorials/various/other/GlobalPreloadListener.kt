package tv.codealong.tutorials.various.other

import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan

/**
 * Глобально для всех тестов
 */
class GlobalPreloadListener : TestExecutionListener {

    private val preloadedClasses = mutableSetOf<String>()

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        println("🔍 Starting global class preloading...")

        // 1. Предзагрузка common классов
        preloadCommonClasses()
        //https://youtu.be/VELjPMxjM9Y?si=gA_LN_QSi6JjkNr9

        // 2. Предзагрузка классов из тестового плана
        preloadTestClasses(testPlan)

        println("✅ Global preloading completed. Total: ${preloadedClasses.size} classes")
    }

    private fun preloadCommonClasses() {
        val commonClasses = listOf(
            "org.junit.jupiter.api.Test",
            "org.springframework.boot.test.context.SpringBootTest",
            "org.springframework.beans.factory.annotation.Autowired",
            "javax.persistence.Entity",
            "java.util.List",
            "java.util.ArrayList"
        )

        commonClasses.forEach { safePreloadClass(it) }
    }

    private fun preloadTestClasses(testPlan: TestPlan) {
        testPlan.roots.forEach { testIdentifier ->
            preloadClassesFromIdentifier(testIdentifier, testPlan)
        }
    }

    private fun preloadClassesFromIdentifier(identifier: TestIdentifier, testPlan: TestPlan) {
        // Получаем класс из источника теста
        val testSource = identifier.source.orElse(null)
        if (testSource is ClassSource) {
            val testClass = testSource.javaClass
            safePreloadClass(testClass.name)
            println("   📦 Preloading test class: ${testClass.simpleName}")
        }

        // Рекурсивно обрабатываем детей
        testPlan.getChildren(identifier).forEach { childId ->
            preloadClassesFromIdentifier(childId, testPlan)
        }
    }

    private fun safePreloadClass(className: String) {
        if (className.startsWith("java.") || className.startsWith("kotlin.")) {
            return
        }

        if (preloadedClasses.contains(className)) {
            return
        }

        try {
            Class.forName(className)
            preloadedClasses.add(className)
        } catch (e: ClassNotFoundException) {
            println("   ⚠️  Class not found: $className")
        } catch (e: Exception) {
            println("   ⚠️  Error loading $className: ${e.message}")
        }
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        println("📊 Preloading statistics: ${preloadedClasses.size} classes loaded")
    }
}