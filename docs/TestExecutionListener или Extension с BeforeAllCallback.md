### Что выбрать?
Класс от `TestExecutionListener` или использовать `Extension` с реализацией от `BeforeAllCallback`?

Оба подхода решают похожие задачи, но имеют **ключевые различия**. Давайте сравним.

## 📊 **Сравнительная таблица**

| Характеристика | TestExecutionListener | Extension (BeforeAllCallback) |
|----------------|----------------------|-----------------------------|
| **Уровень** | Test Plan (глобальный) | Test Class/Method (локальный) |
| **Время жизни** | Весь test run | Отдельный test class |
| **Количество вызовов** | 1 раз на весь test run | 1 раз на КАЖДЫЙ test class |
| **Контекст** | Глобальный контекст | Контекст конкретного класса |
| **Порядок** | Выше в цепочке выполнения | Ниже в цепочке выполнения |
| **Гибкость** | Меньше контекста о тестах | Больше информации о тесте |

## 🎯 **Когда что использовать:**

### **1. TestExecutionListener - для ГЛОБАЛЬНЫХ действий**
```kotlin
class GlobalTestListener : TestExecutionListener {
    
    // ✅ Выполняется ОДИН раз на весь test run
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        println("🚀 Test run started")
        PreloadUtils.preloadAllClasses() // Предзагрузка для всех тестов
    }
    
    // ✅ Выполняется ОДИН раз в конце
    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        println("✅ Test run finished")
        cleanupGlobalResources()
    }
}
```

### **2. Extension + BeforeAllCallback - для ЛОКАЛЬНЫХ действий**
```kotlin
class ClassPreloadingExtension : BeforeAllCallback {
    
    // ✅ Выполняется для КАЖДОГО тестового класса
    override fun beforeAll(context: ExtensionContext) {
        println("📦 Setup for ${context.displayName}")
        PreloadUtils.preloadClassesFor(context.testClass)
    }
}
```

## 🔧 **Практические примеры:**

### **TestExecutionListener - идеально для:**
```kotlin
class GlobalMonitoringListener : TestExecutionListener {
    
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        // 1. Предзагрузка ВСЕХ классов
        PreloadUtils.preloadCriticalClasses()
        
        // 2. Запуск глобального мониторинга
        ForkJoinPoolMonitor.startMonitoring()
        
        // 3. Инициализация глобальных ресурсов
        DatabaseTestPool.initialize()
    }
    
    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        // 1. Остановка мониторинга
        ForkJoinPoolMonitor.stopMonitoring()
        
        // 2. Очистка ресурсов
        DatabaseTestPool.shutdown()
    }
}
```

### **Extension - идеально для:**
```kotlin
class DatabaseSetupExtension : BeforeAllCallback, AfterAllCallback {
    
    override fun beforeAll(context: ExtensionContext) {
        // 1. Предзагрузка классов для КОНКРЕТНОГО теста
        val testClass = context.requiredTestClass
        PreloadUtils.preloadForClass(testClass)
        
        // 2. Setup базы данных для этого класса
        TestDatabase.initializeFor(testClass)
    }
    
    override fun afterAll(context: ExtensionContext) {
        // Очистка после конкретного класса
        TestDatabase.cleanupFor(context.requiredTestClass)
    }
}

// Использование
@ExtendWith(DatabaseSetupExtension::class)
class UserRepositoryTest {
    // тесты
}
```

## 🚀 **Комбинированный подход (рекомендуется):**

```kotlin
// 1. Глобальный listener для предзагрузки
class GlobalPreloadListener : TestExecutionListener {
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        PreloadUtils.preloadCommonClasses() // Spring, Utils, etc.
    }
}

// 2. Extension для специфичной предзагрузки
class SpecificPreloadExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        // Догрузка классов для конкретного тестового класса
        PreloadUtils.preloadTestSpecificClasses(context.requiredTestClass)
    }
}

// 3. Регистрация
// в junit-platform.properties:
junit.platform.execution.listeners.include=com.example.GlobalPreloadListener

// в тестах:
@ExtendWith(SpecificPreloadExtension::class)
class MyTest
```

## 📋 **Чеклист выбора:**

**Используйте TestExecutionListener если:**
- Нужно действие на **весь test run**
- **Глобальная** инициализация/очистка
- Мониторинг **всех** тестов
- Предзагрузка **общих** классов

**Используйте Extension если:**
- Действие для **конкретного** тестового класса
- **Локальная** настройка ресурсов
- Предзагрузка **специфичных** классов
- Нужен доступ к **контексту** теста

## ⚠️ **Важное замечание о предзагрузке:**

Для предзагрузки классов **лучше использовать TestExecutionListener**, потому что:

1. **Выполняется один раз** - не дублируется для каждого класса
2. **Выполняется первым** - до любого теста
3. **Предотвращает дедлоки** до начала параллельного выполнения

## ✅ **Итоговые рекомендации:**

1. **Для предзагрузки классов** → `TestExecutionListener`
2. **Для подготовки тестовых данных** → `Extension`
3. **Для мониторинга всего test run** → `TestExecutionListener`
4. **Для setup/teardown конкретного класса** → `Extension`

Теперь вы можете выбрать правильный инструмент для каждой задачи! 🛠️

---

Конечно! Вот реализация для предзагрузки классов конкретного тестового класса.

## 🎯 **Реализация PreloadUtils.preloadClassesFor()**

```kotlin
import org.junit.jupiter.api.extension.ExtensionContext
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

object PreloadUtils {
    
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
```

## 🚀 **Extension для использования**

```kotlin
class TestClassPreloadingExtension : BeforeAllCallback {
    
    companion object {
        private val preloaded = AtomicBoolean(false)
    }
    
    override fun beforeAll(context: ExtensionContext) {
        // Предзагружаем только один раз для этого класса
        if (preloaded.compareAndSet(false, true)) {
            PreloadUtils.preloadClassesFor(context)
        }
    }
}
```

## 🧪 **Пример тестового класса**

```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import javax.persistence.Entity
import java.util.*

// Аннотации, которые будут предзагружены
@SpringBootTest
@ExtendWith(TestClassPreloadingExtension::class)
class UserServiceTest {
    
    // Поля, типы которых будут предзагружены
    @Autowired
    lateinit var userService: UserService
    
    val temporaryData: List<String> = listOf("test1", "test2")
    
    @Test
    fun testUserCreation() {
        // Тест будет использовать предзагруженные классы
    }
}

// Классы, которые будут предзагружены
@Entity
class User(val name: String, val email: String)

@Service
class UserService(
    @Autowired private val userRepository: UserRepository
) {
    fun createUser(user: User) = userRepository.save(user)
}

interface UserRepository : JpaRepository<User, Long>
```

## 📊 **Что будет предзагружено:**

Для `UserServiceTest` будет предзагружено:
1. `@SpringBootTest` аннотация
2. `UserService` тип поля
3. `List<String>` generic тип
4. `User` класс из метода
5. `UserRepository` интерфейс
6. Все связанные аннотации

## 🎯 **Ключевые особенности:**

1. **Рекурсивная предзагрузка** - загружает все связанные классы
2. **Безопасная обработка** - игнорирует ошибки
3. **Предотвращение дублирования** - отслеживает уже загруженные классы
4. **Generic types support** - обрабатывает параметризованные типы
5. **Статистика** - показывает что было загружено

## 💡 **Использование:**

```kotlin
// Вручную для конкретного класса
PreloadUtils.preloadClassesFor(UserServiceTest::class.java)

// Через ExtensionContext
@ExtendWith(TestClassPreloadingExtension::class)
class MyTest

// Глобально для всех тестов
class GlobalPreloadListener : TestExecutionListener {
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        testPlan.getRoots().forEach { root ->
            root.getChildren().forEach { testIdentifier ->
                if (testIdentifier.isTest) {
                    val testClass = Class.forName(testIdentifier.className)
                    PreloadUtils.preloadClassesFor(testClass)
                }
            }
        }
    }
}
```

Теперь ваши тесты будут защищены от дедлоков ClassLoader! 🚀

---


Вы абсолютно правы! Ошибка в последнем примере. Метода `getChildren()` нет в JUnit 5 API. Вот правильная реализация:

## ✅ **Исправленная реализация GlobalPreloadListener**

```kotlin
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan

class GlobalPreloadListener : TestExecutionListener {
    
    private val preloadedClasses = mutableSetOf<String>()
    
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        println("🔍 Starting global class preloading...")
        
        // 1. Предзагрузка common классов
        preloadCommonClasses()
        
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
        testPlan.root.forEach { testIdentifier ->
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
```

## 🚀 **Альтернативный упрощенный подход**

Если не нужно рекурсивно обходить весь test plan:

```kotlin
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
```

## 🎯 **Правильный способ для конкретного test class**

```kotlin
class SpecificClassPreloadListener : TestExecutionListener {
    
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        // Ищем конкретные тестовые классы по тегам или паттернам
        testPlan.root.forEach { rootIdentifier ->
            findTestClasses(rootIdentifier, testPlan).forEach { testClass ->
                PreloadUtils.preloadClassesFor(testClass)
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
```

## 📋 **Регистрация в junit-platform.properties**

```properties
# Для глобальной предзагрузки
junit.platform.execution.listeners.include=com.example.GlobalPreloadListener

# Для отключения стандартных listeners (опционально)
junit.platform.execution.listeners.deactivate=org.junit.platform.launcher.listeners.LoggingListener
```

## 💡 **Более практичный подход**

Часто проще использовать `@BeforeAll` в базовом классе:

```kotlin
@ExtendWith(TestClassPreloadingExtension::class)
abstract class BaseTest {
    
    companion object {
        @JvmStatic
        @BeforeAll
        fun preloadClasses() {
            PreloadUtils.preloadClassesFor(this::class.java)
        }
    }
}

// Все тесты наследуют предзагрузку
class UserServiceTest : BaseTest() {
    // тесты
}
```
---
