package tv.codealong.tutorials.various.other

import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan

/**
 * Правильный способ для конкретного test class
 */
class SpecificClassPreloadListener : TestExecutionListener {

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        // Ищем конкретные тестовые классы по тегам или паттернам
        testPlan.roots.forEach { rootIdentifier ->
            findTestClasses(rootIdentifier, testPlan).forEach { testClass ->
                PreloadUtilsExtended.preloadClassesFor(testClass)
            }
        }
    }

    private fun findTestClasses(
        identifier: TestIdentifier,
        testPlan: TestPlan
    ): List<Class<*>> {
        val classes = mutableListOf<Class<*>>()

        // Проверяем, является ли это тестовым классом
        identifier.source.ifPresent { source ->
            if (source is ClassSource) {
                classes.add(source.javaClass)
            }
        }

        // Рекурсивно проверяем детей
        testPlan.getChildren(identifier).forEach { childId ->
            classes.addAll(findTestClasses(childId, testPlan))
        }

        return classes
    }
}