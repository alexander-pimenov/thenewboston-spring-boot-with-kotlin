package tv.codealong.tutorials.various.other

import org.junit.jupiter.api.extension.ExtensionContext
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.*

/**
 * Ключевые особенности:
 * - Рекурсивная предзагрузка - загружает все связанные классы
 * - Безопасная обработка - игнорирует ошибки
 * - Предотвращение дублирования - отслеживает уже загруженные классы
 * - Generic types support - обрабатывает параметризованные типы
 * - Статистика - показывает что было загружено
 */
object PreloadUtilsExtended {
    private val preloadedClasses = Collections.synchronizedSet(mutableSetOf<Class<*>>())

    /**
     * Предзагружает все классы, связанные с тестовым классом
     */
    fun preloadClassesFor(testClass: Class<*>) {
        val className = testClass.name
        if (preloadedClasses.contains(testClass)) {
            return
        }

        println("🔍 Preloading classes for: ${testClass.simpleName}")

        try {
            // 1. Предзагрузка самого тестового класса
            safePreloadClass(testClass)

            // 2. Предзагрузка классов из аннотаций
            preloadAnnotationClasses(testClass)

            // 3. Предзагрузка классов из полей
            preloadFieldClasses(testClass)

            // 4. Предзагрузка классов из методов
            preloadMethodClasses(testClass)

            // 5. Предзагрузка классов из наследования
            preloadSuperclassAndInterfaces(testClass)

            preloadedClasses.add(testClass)
            println("✅ Preloaded ${preloadedClasses.size} classes for ${testClass.simpleName}")

        } catch (e: Exception) {
            println("⚠️  Error preloading classes for ${testClass.simpleName}: ${e.message}")
        }
    }

    private fun preloadAnnotationClasses(testClass: Class<*>) {
        testClass.annotations.forEach { annotation ->
            safePreloadClass(annotation.annotationClass.java)

            // Рекурсивно загружаем классы из аннотаций аннотаций
            annotation.annotationClass.java.annotations.forEach {
                safePreloadClass(it.annotationClass.java)
            }
        }
    }

    private fun preloadFieldClasses(testClass: Class<*>) {
        testClass.declaredFields.forEach { field ->
            // Предзагрузка типа поля
            safePreloadClass(field.type)

            // Предзагрузка generic типов
            preloadGenericTypes(field.genericType)

            // Предзагрузка аннотаций поля
            field.annotations.forEach { safePreloadClass(it.annotationClass.java) }
        }
    }

    private fun preloadMethodClasses(testClass: Class<*>) {
        testClass.declaredMethods.forEach { method ->
            // Предзагрузка возвращаемого типа
            safePreloadClass(method.returnType)

            // Предзагрузка типов параметров
            method.parameterTypes.forEach { safePreloadClass(it) }

            // Предзагрузка generic типов
            preloadGenericTypes(method.genericReturnType)
            method.genericParameterTypes.forEach { preloadGenericTypes(it) }

            // Предзагрузка аннотаций метода
            method.annotations.forEach { safePreloadClass(it.annotationClass.java) }
        }
    }

    private fun preloadSuperclassAndInterfaces(testClass: Class<*>) {
        // Предзагрузка суперкласса
        testClass.superclass?.let { safePreloadClass(it) }

        // Предзагрузка интерфейсов
        testClass.interfaces.forEach { safePreloadClass(it) }
    }

    private fun preloadGenericTypes(genericType: Type) {
        try {
            when (genericType) {
                is Class<*> -> safePreloadClass(genericType)
                is java.lang.reflect.ParameterizedType -> {
                    safePreloadClass(genericType.rawType as Class<*>)
                    genericType.actualTypeArguments.forEach {
                        if (it is Class<*>) {
                            safePreloadClass(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Игнорируем ошибки generic types
        }
    }

    private fun safePreloadClass(clazz: Class<*>) {
        try {
            if (clazz.name.startsWith("java.") || clazz.name.startsWith("kotlin.")) {
                return // Пропускаем системные классы
            }

            if (!preloadedClasses.contains(clazz)) {
                Class.forName(clazz.name)
                preloadedClasses.add(clazz)
                println("   📦 Preloaded: ${clazz.simpleName}")
            }
        } catch (e: ClassNotFoundException) {
            println("   ⚠️  Class not found: ${clazz.name}")
        } catch (e: Exception) {
            println("   ⚠️  Error loading ${clazz.name}: ${e.message}")
        }
    }

    /**
     * Предзагрузка для конкретного ExtensionContext
     */
    fun preloadClassesFor(context: ExtensionContext) {
        val testClass = context.requiredTestClass
        preloadClassesFor(testClass)
    }

    /**
     * Получение статистики
     */
    fun getPreloadStatistics(): String {
        return "Preloaded ${preloadedClasses.size} classes: " +
                preloadedClasses.joinToString { it.simpleName }
    }
}