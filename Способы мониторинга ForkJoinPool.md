Отличный вопрос! Для мониторинга ForkJoinPool при параллельном запуске тестов JUnit 5 есть несколько мощных инструментов.

## 🔍 **Способы мониторинга ForkJoinPool**

### 1. **JVM Built-in мониторинг через JMX**
```bash
# Запуск тестов с JMX
./mvnw test -Djava.rmi.server.hostname=localhost \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9010 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false
```

**Подключение:**
1. Запустите `jconsole` или `jvisualvm`
2. Подключитесь к процессу
3. Перейдите в **MBeans** → **java.util.concurrent** → **ForkJoinPool**

### 2. **Thread Dump во время выполнения тестов**
```bash
# Найти PID процесса тестов
jps -l | grep maven # или gradle

# Сделать дамп потоков
jstack <PID> > thread_dump.txt

# Или несколько дампов с интервалом
for i in {1..5}; do jstack <PID> > dump_$i.txt; sleep 2; done
```

### 3. **Программный мониторинг из тестов**
```kotlin
import java.lang.management.ManagementFactory
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

class ForkJoinPoolMonitor {

    companion object {
        private val lock = ReentrantLock()
        private val monitoringThread by lazy { startMonitoring() }

        @JvmStatic
        fun startMonitoring() = thread(name = "FJP-Monitor", isDaemon = true) {
            while (true) {
                monitorForkJoinPool()
                Thread.sleep(3000) // Каждые 3 секунды
            }
        }

        @JvmStatic
        fun monitorForkJoinPool() {
            lock.withLock {
                val threadBean = ManagementFactory.getThreadMXBean()
                val forkJoinPool = ForkJoinPool.commonPool()

                println("=== ForkJoinPool Monitor ===")
                println("Parallelism: ${forkJoinPool.parallelism}")
                println("Pool size: ${forkJoinPool.poolSize}")
                println("Active threads: ${forkJoinPool.activeThreadCount}")
                println("Queued tasks: ${forkJoinPool.queuedTaskCount}")
                println("Steal count: ${forkJoinPool.stealCount}")

                // Анализ состояния потоков
                val threadIds = threadBean.allThreadIds
                threadBean.getThreadInfo(threadIds).forEach { info ->
                    if (info?.threadName?.startsWith("ForkJoinPool") == true) {
                        println("Thread ${info.threadName}: " +
                                "state=${info.threadState}, " +
                                "blocked=${info.blockedTime}ms, " +
                                "waited=${info.waitedTime}ms")
                    }
                }
                println("============================")
            }
        }
    }
}
```

**Использование в тестах:**
```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.CONCURRENT)
class ConcurrentTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            ForkJoinPoolMonitor.startMonitoring()
        }
    }

    @Test
    fun test1() {
        // ваш тест
    }

    @Test
    fun test2() {
        // ваш тест
    }
}
```

### 4. **JUnit 5 Listeners для мониторинга**
```kotlin
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier

class ThreadMonitoringListener : TestExecutionListener {
    
    override fun executionStarted(testIdentifier: TestIdentifier) {
        println("Test started: ${testIdentifier.displayName}")
        ForkJoinPoolMonitor.monitorForkJoinPool()
    }

    override fun executionFinished(
        testIdentifier: TestIdentifier, 
        testExecutionResult: TestExecutionResult
    ) {
        println("Test finished: ${testIdentifier.displayName}")
        ForkJoinPoolMonitor.monitorForkJoinPool()
    }
}
```

**В `src/test/resources/junit-platform.properties`:**
```properties
junit.platform.execution.listeners.deactivate=org.junit.platform.launcher.listeners.LoggingListener
junit.platform.execution.listeners.include=com.example.ThreadMonitoringListener
```

### 5. **Flight Recorder для глубокого анализа**
```bash
# Запуск тестов с JFR
./mvnw test -XX:+FlightRecorder \
-XX:StartFlightRecording=delay=10s,duration=60s,filename=test_recording.jfr

# Анализ записи
jfr print test_recording.jfr | grep ForkJoinPool
# или
jmc test_recording.jfr
```

## 🎯 **Что искать в мониторинге:**

### **Признаки голодания:**
```plaintext
Thread ForkJoinPool-1-worker-3: state=WAITING, waited=15000ms
Thread ForkJoinPool-1-worker-5: state=BLOCKED, blocked=20000ms
```

### **Признаки дедлока:**
```plaintext
Found one Java-level deadlock:
=============================
"ForkJoinPool-1-worker-1":
  waiting to lock monitor 0x00007f...
```

### **Здоровый пул:**
```plaintext
Parallelism: 4
Pool size: 4  
Active threads: 2-4  # Колеблется в разумных пределах
Queued tasks: 0-10   # Не должно расти бесконечно
```

## 📊 **Практический пример диагностики:**

```kotlin
@Test
fun testWithDeadlockDetection() {
    val threadBean = ManagementFactory.getThreadMXBean()
    
    // Проверка на дедлоки перед тестом
    val deadlockedThreads = threadBean.findDeadlockedThreads()
    if (deadlockedThreads != null) {
        fail("Deadlock detected before test execution!")
    }

    // Ваш тест...

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
```

## 🛠️ **Рекомендуемый подход:**

1. **Начните с простого мониторинга** через `jstack` и JMX
2. **Добавьте программный мониторинг** для сложных случаев
3. **Используйте JFR** для глубокого анализа в production-like среде
4. **Настройте CI мониторинг** для автоматического обнаружения проблем

Теперь вы сможете видеть всю картину работы ForkJoinPool во время параллельного выполнения тестов! 🚀

---

Именно! Но нужно сделать несколько важных шагов правильно. Вот подробное объяснение:

## 🛠️ **Шаг 1: Создание класса Listener**

```kotlin
package com.yourpackage.listeners

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import java.lang.management.ManagementFactory
import java.util.concurrent.ForkJoinPool

class ThreadMonitoringListener : TestExecutionListener {
    
    private val forkJoinPool = ForkJoinPool.commonPool()

    override fun executionStarted(testIdentifier: TestIdentifier) {
        if (testIdentifier.isTest) {
            printPoolStatus("Test STARTED: ${testIdentifier.displayName}")
        }
    }

    override fun executionFinished(
        testIdentifier: TestIdentifier, 
        testExecutionResult: TestExecutionResult
    ) {
        if (testIdentifier.isTest) {
            printPoolStatus("Test FINISHED: ${testIdentifier.displayName}")
            checkForDeadlocks()
        }
    }

    private fun printPoolStatus(context: String) {
        println("\n=== $context ===")
        println("ForkJoinPool status:")
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
```

## 📁 **Шаг 2: Размещение файла настроек**

Создайте файл: `src/test/resources/junit-platform.properties`

```properties
# Включение нашего listener
junit.platform.execution.listeners.include=com.yourpackage.listeners.ThreadMonitoringListener

# Отключение стандартных listeners (опционально)
junit.platform.execution.listeners.deactivate=org.junit.platform.launcher.listeners.LoggingListener

# Настройки параллельного выполнения
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=4
```

## 🔧 **Шаг 3: Альтернатива - регистрация через ServiceLoader**

Если свойства не работают, создайте файл:
`src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener`

С содержимым:
```
com.yourpackage.listeners.ThreadMonitoringListener
```

## 🧪 **Шаг 4: Пример тестов для проверки**

```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.concurrent.TimeUnit

@Execution(ExecutionMode.CONCURRENT)
class ConcurrentTestExample {

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
}
```

## 🚀 **Шаг 5: Запуск и наблюдение**

```bash
# Запуск тестов
./mvnw test

# Или с дополнительной информацией
./mvnw test -Dorg.slf4j.simpleLogger.log.org.junit=debug
```

**Ожидаемый вывод:**
```
=== Test STARTED: test1() ===
ForkJoinPool status:
  Parallelism: 4
  Pool size: 4
  Active threads: 1
  Queued tasks: 0
  Steal count: 0
======================

=== Test FINISHED: test1() ===
ForkJoinPool status:
  Parallelism: 4
  Pool size: 4  
  Active threads: 3
  Queued tasks: 2
  Steal count: 1
======================
```

## ⚠️ **Возможные проблемы и решения:**

### **1. Listener не работает:**
```properties
# Проверьте package в properties файле
junit.platform.execution.listeners.include=com.yourpackage.listeners.ThreadMonitoringListener
```

### **2. Не видит ресурсы:**
Убедитесь, что `src/test/resources/` добавлен в classpath.

### **3. Для Maven:**
```xml
<build>
    <testResources>
        <testResource>
            <directory>src/test/resources</directory>
        </testResource>
    </testResources>
</build>
```

### **4. Для Gradle:**
```kotlin
sourceSets {
    test {
        resources.srcDir("src/test/resources")
    }
}
```

## 🎯 **Что вы увидите:**

- **Старт/завершение** каждого теста
- **Статус ForkJoinPool** в реальном времени
- **Обнаружение дедлоков** автоматически
- **Статистику** по активным потокам и задачам

Теперь вы сможете наблюдать за работой ForkJoinPool во время выполнения параллельных тестов!

---

Отличный вопрос! Размещение класса `ThreadMonitoringListener` важно для его корректной работы. Вот правильная структура:

## 📁 **Стандартная структура Maven/Gradle проекта:**

```
your-project/
├── src/
│   ├── main/
│   │   ├── kotlin/          # Основной код
│   │   └── resources/       # Основные ресурсы
│   └── test/
│       ├── kotlin/          # Тесты
│       │   └── com/
│       │       └── yourpackage/
│       │           └── listeners/
│       │               └── ThreadMonitoringListener.kt  # ← ВОТ ЗДЕСЬ!
│       └── resources/       # Ресурсы для тестов
│           ├── junit-platform.properties
│           └── META-INF/
│               └── services/
│                   └── org.junit.platform.launcher.TestExecutionListener
```

## 🎯 **Конкретное местоположение:**

### **1. Для Kotlin проекта:**
```
src/test/kotlin/com/yourcompany/yourproject/listeners/ThreadMonitoringListener.kt
```

### **2. Для Java проекта:**
```
src/test/java/com/yourcompany/yourproject/listeners/ThreadMonitoringListener.java
```

## 🛠️ **Пошаговое создание:**

**Шаг 1: Создайте package structure:**
```bash
# В корне проекта
mkdir -p src/test/kotlin/com/yourcompany/yourproject/listeners
```

**Шаг 2: Создайте файл Listener:**
`src/test/kotlin/com/yourcompany/yourproject/listeners/ThreadMonitoringListener.kt`

```kotlin
package com.yourcompany.yourproject.listeners

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import java.lang.management.ManagementFactory
import java.util.concurrent.ForkJoinPool

class ThreadMonitoringListener : TestExecutionListener {
    
    private val forkJoinPool = ForkJoinPool.commonPool()

    override fun executionStarted(testIdentifier: TestIdentifier) {
        if (testIdentifier.isTest) {
            printPoolStatus("Test STARTED: ${testIdentifier.displayName}")
        }
    }

    override fun executionFinished(
        testIdentifier: TestIdentifier, 
        testExecutionResult: TestExecutionResult
    ) {
        if (testIdentifier.isTest) {
            printPoolStatus("Test FINISHED: ${testIdentifier.displayName}")
            checkForDeadlocks()
        }
    }

    private fun printPoolStatus(context: String) {
        println("\n=== $context ===")
        println("ForkJoinPool status:")
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
```

**Шаг 3: Создайте файл настроек:**
`src/test/resources/junit-platform.properties`

```properties
junit.platform.execution.listeners.include=com.yourcompany.yourproject.listeners.ThreadMonitoringListener
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
```

**Шаг 4: (Опционально) ServiceLoader конфиг:**
`src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener`

```
com.yourcompany.yourproject.listeners.ThreadMonitoringListener
```

## 🔍 **Проверка правильности размещения:**

**Убедитесь, что:**
1. Файл находится в `src/test/` (не в `src/main/`)
2. Package в файле совпадает с путем папок
3. Имя package в `junit-platform.properties` правильное
4. Проект пересобран после изменений

## ⚠️ **Частые ошибки:**

1. **Listener в `src/main/`** - не будет работать для тестов
2. **Несовпадение package** - проверьте путь в properties файле
3. **Отсутствие resources** - убедитесь, что папка resources добавлена в classpath

## 🚀 **Проверка работы:**

```bash
# Для Maven
./mvnw clean test

# Для Gradle  
./gradlew clean test

# С подробным выводом
./mvnw test -Dorg.slf4j.simpleLogger.log.org.junit=info
```

Если все сделано правильно, вы увидите вывод мониторинга в консоли при запуске тестов!

---

Вы абсолютно правы! Это **критическая проблема** в том коде, который я показал ранее. `while(true)` создаст **бесконечный цикл**, который не остановится после завершения тестов.

## 🚫 **Проблема в исходном коде:**

```kotlin
fun startMonitoring() = thread(name = "FJP-Monitor", isDaemon = true) {
    while (true) { // ❌ БЕСКОНЕЧНЫЙ ЦИКЛ!
        monitorForkJoinPool()
        Thread.sleep(3000)
    }
}
```

## ✅ **Исправленная версия:**

Вот правильная реализация с безопасной остановкой:

### **1. Версия с флагом остановки**

```kotlin
import java.lang.management.ManagementFactory
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

object ForkJoinPoolMonitor {
    private val isRunning = AtomicBoolean(false)
    private var monitoringThread: Thread? = null
    
    @JvmStatic
    fun startMonitoring(intervalMs: Long = 3000) {
        if (isRunning.get()) return
        
        isRunning.set(true)
        monitoringThread = thread(name = "FJP-Monitor", isDaemon = true) {
            while (isRunning.get()) { // ✅ Проверяем флаг
                monitorForkJoinPool()
                Thread.sleep(intervalMs)
            }
            println("ForkJoinPool monitoring stopped")
        }
    }
    
    @JvmStatic
    fun stopMonitoring() {
        isRunning.set(false)
        monitoringThread?.interrupt()
        monitoringThread = null
    }
    
    @JvmStatic
    fun monitorForkJoinPool() {
        val forkJoinPool = ForkJoinPool.commonPool()
        val threadBean = ManagementFactory.getThreadMXBean()
        
        println("\n=== ForkJoinPool Monitor ===")
        println("Parallelism: ${forkJoinPool.parallelism}")
        println("Pool size: ${forkJoinPool.poolSize}")
        println("Active threads: ${forkJoinPool.activeThreadCount}")
        println("Queued tasks: ${forkJoinPool.queuedTaskCount}")
        println("Steal count: ${forkJoinPool.stealCount}")
        
        // Анализ состояния потоков
        threadBean.allThreadIds
            .mapNotNull { threadBean.getThreadInfo(it) }
            .filter { it.threadName.startsWith("ForkJoinPool") }
            .forEach { info ->
                println("Thread ${info.threadName}: " +
                        "state=${info.threadState}, " +
                        "blocked=${info.blockedTime}ms, " +
                        "waited=${info.waitedTime}ms")
            }
    }
}
```

### **2. Версия с автоматической остановкой по таймауту**

```kotlin
object SafeForkJoinPoolMonitor {
    private var monitoringThread: Thread? = null
    
    @JvmStatic
    fun startMonitoring(durationMinutes: Int = 5) {
        stopMonitoring() // Останавливаем предыдущий, если был
        
        monitoringThread = thread(name = "FJP-Monitor", isDaemon = true) {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + durationMinutes * 60 * 1000
            
            while (System.currentTimeMillis() < endTime) { // ✅ По таймауту
                monitorForkJoinPool()
                try {
                    Thread.sleep(3000)
                } catch (e: InterruptedException) {
                    println("Monitoring interrupted")
                    break
                }
            }
            println("ForkJoinPool monitoring completed")
        }
    }
    
    @JvmStatic
    fun stopMonitoring() {
        monitoringThread?.interrupt()
        monitoringThread = null
    }
}
```

### **3. Интеграция с JUnit 5 Lifecycle**

```kotlin
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class ForkJoinPoolMonitoringExtension : BeforeAllCallback, AfterAllCallback {
    
    override fun beforeAll(context: ExtensionContext) {
        ForkJoinPoolMonitor.startMonitoring()
        println("ForkJoinPool monitoring started")
    }
    
    override fun afterAll(context: ExtensionContext) {
        ForkJoinPoolMonitor.stopMonitoring()
        println("ForkJoinPool monitoring stopped")
    }
}

// Использование в тестах
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ForkJoinPoolMonitoringExtension::class)
class MyConcurrentTests {
    
    @Test
    fun test1() {
        // ваш тест
    }
}
```

### **4. Автоматическая остановка при завершении JVM**

```kotlin
object SmartForkJoinPoolMonitor {
    init {
        // Автоматическая остановка при shutdown JVM
        Runtime.getRuntime().addShutdownHook(thread(isDaemon = true) {
            stopMonitoring()
        })
    }
    
    // ... остальная реализация как выше
}
```

## 🎯 **Правильное использование в тестах:**

### **Способ 1: Через @BeforeAll/@AfterAll**

```kotlin
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ConcurrentTests {
    
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            ForkJoinPoolMonitor.startMonitoring()
        }
        
        @JvmStatic
        @AfterAll  
        fun cleanup() {
            ForkJoinPoolMonitor.stopMonitoring()
        }
    }
    
    @Test
    fun test1() {
        // тест
    }
}
```

### **Способ 2: Через Test Execution Listener**

```kotlin
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier

class MonitoringTestListener : TestExecutionListener {
    
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        ForkJoinPoolMonitor.startMonitoring()
    }
    
    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        ForkJoinPoolMonitor.stopMonitoring()
    }
}
```

## 📊 **Что теперь будет в выводе:**

```
=== ForkJoinPool Monitor ===
Parallelism: 4
Pool size: 4
Active threads: 2
Queued tasks: 3
Steal count: 5
Thread ForkJoinPool-1-worker-1: state=RUNNABLE, blocked=0ms, waited=0ms
Thread ForkJoinPool-1-worker-2: state=WAITING, blocked=0ms, waited=150ms
====================

... через 3 секунды ...

=== ForkJoinPool Monitor ===
Parallelism: 4  
Pool size: 4
Active threads: 3
Queued tasks: 1
Steal count: 8
====================

ForkJoinPool monitoring stopped // ✅ Корректная остановка!
```

Теперь мониторинг будет **безопасно запускаться и останавливаться** без бесконечных циклов! 🚀

---
