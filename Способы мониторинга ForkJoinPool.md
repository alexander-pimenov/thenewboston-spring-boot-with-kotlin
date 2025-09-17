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

Отличное замечание! Вы абсолютно правы - я оставил голый комментарий, но не объяснил **почему это уже корректное поведение**. Давайте разберем детально.

## 🎯 **Почему это уже корректно:**

```kotlin
} catch (e: InterruptedException) {
    // Корректное завершение по прерыванию
    println("Monitoring interrupted gracefully")
}
```

**Это корректно, потому что:**

1. **Мы выходим из цикла** - исключение прерывает `Thread.sleep()`, поток продолжает выполнение
2. **Дальше идет `finally` блок** - который гарантированно выполнится
3. **Поток завершается естественно** - после окончания метода `run()`

## 🔄 **Что происходит под капотом:**

```kotlin
thread {
    try {
        while (isRunning.get()) {    // 1. Проверка условия
            monitorForkJoinPool()    // 2. Выполняем работу
            Thread.sleep(intervalMs) // 3. Спим → 🚨 INTERRUPTED!
        }
    } catch (e: InterruptedException) {
        // 4. Ловим исключение, выводим сообщение
        println("Monitoring interrupted gracefully")
    } finally {
        // 5. Гарантированно выполняем cleanup
        println("ForkJoinPool monitoring stopped")
    }
    // 6. Поток завершается ← ✅ КОРРЕКТНО!
}
```

## ⚠️ **Когда нужно ВОССТАНАВЛИВАТЬ interrupted status:**

Восстановление статуса нужно, **если вы не можете сразу завершить работу** и должны передать сигнал прерывания дальше:

```kotlin
} catch (e: InterruptedException) {
    // СЛУЧАЙ 1: Не можем сразу завершиться → передаем дальше
    Thread.currentThread().interrupt() // ← ВОССТАНАВЛИВАЕМ статус
    doSomeCleanupThatMightAlsoCheckInterruption()
    
    // СЛУЧАЙ 2: Работаем с блокирующими операциями
    Thread.currentThread().interrupt()
    throw RuntimeException("Operation cancelled", e)
}
```

## ✅ **В вашем случае это НЕ нужно, потому что:**

1. **Вы сразу завершаете поток** после прерывания
2. **Нет дальнейших блокирующих операций**
3. **Поток просто заканчивает работу**

## 🚀 **Полная версия с объяснением:**

```kotlin
object ForkJoinPoolMonitor {
    // ...
    
    @JvmStatic
    fun startMonitoring(intervalMs: Long = 3000) {
        if (isRunning.get()) return
        
        isRunning.set(true)
        monitoringThread = thread(name = "FJP-Monitor", isDaemon = true) {
            try {
                while (isRunning.get()) {
                    monitorForkJoinPool()
                    
                    // Sleep может быть прерван вызовом thread.interrupt()
                    Thread.sleep(intervalMs)
                }
            } catch (e: InterruptedException) {
                // ✅ КОРРЕКТНО: Мы ловим прерывание и просто завершаемся
                // НЕ восстанавливаем статус, потому что поток завершается
                println("Monitoring interrupted gracefully")
            } finally {
                // Гарантированный cleanup
                println("ForkJoinPool monitoring stopped")
                isRunning.set(false)
            }
        }
    }
    
    @JvmStatic
    fun stopMonitoring() {
        isRunning.set(false)
        monitoringThread?.interrupt() // Посылаем сигнал прерывания
        monitoringThread = null
    }
}
```

## 📊 **Когда что использовать:**

| Ситуация | Обработка |
|----------|-----------|
| **Немедленное завершение** | `catch { println(); }` ← ВАШ СЛУЧАЙ |
| **Постепенное завершение** | `catch { interrupt(); cleanup(); }` |
| **Передача прерывания** | `catch { interrupt(); throw ... }` |

## 🎯 **Итог:**

Ваш текущий код **уже корректен** для сценария немедленного завершения. Восстановление статуса нужно только если поток должен продолжить работу и проверять прерывание в другом месте.

**Так правильно:** ✅
```kotlin
} catch (e: InterruptedException) {
    println("Monitoring interrupted gracefully")
    // Просто завершаемся - статус не нужен
}
```

Теперь исключение будет обрабатываться "красиво" без stack trace в логах! 🚀

---

Отличные вопросы! Давайте разберемся по порядку.

## 📦 **1. Что лучше: Extension или Object?**

### **ClassPreloadingExtension - лучше!**
**Почему:** Он гарантирует **однократное выполнение** на весь test run.

```kotlin
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class ClassPreloadingExtension : BeforeAllCallback {
    
    companion object {
        @Volatile
        private var preloaded = false
        private val lock = Any()
    }
    
    override fun beforeAll(context: ExtensionContext) {
        if (!preloaded) {
            synchronized(lock) {
                if (!preloaded) {
                    println("⏳ Preloading classes...")
                    PreloadUtils.preloadCriticalClasses()
                    preloaded = true
                    println("✅ Classes preloaded")
                }
            }
        }
    }
}
```

### **Object ClassPreloader - риск двойного выполнения**
Может вызваться несколько раз в параллельных тестах.

## 🔒 **2. Static блоки в Kotlin**

**В Kotlin нет точного аналога `static {}`, но есть:**

### **а) Companion object + `init` блок**
```kotlin
class MyClass {
    companion object {
        init {
            // Аналог static {} - выполняется при первом обращении к классу
            println("This runs like static block")
        }
    }
}
```

### **б) Top-level initialization**
```kotlin
// Файл MyUtils.kt
val initialized = run {
    println("This runs on class loading")
    "initial value"
}
```

### **в) `@JvmStatic` + `init`**
```kotlin
class MyClass {
    companion object {
        @JvmStatic
        val MY_CONSTANT = computeValue()
        
        private fun computeValue(): String {
            println("Like static block")
            return "value"
        }
    }
}
```

## 🚀 **3. Идеальное решение для предзагрузки**

### **Шаг 1: Создайте Utilities**
```kotlin
object PreloadUtils {
    
    private val preloaded = AtomicBoolean(false)
    
    fun preloadCriticalClasses() {
        if (preloaded.getAndSet(true)) return
        
        // 1. Spring компоненты
        preloadSpringClasses()
        
        // 2. Утилитные классы
        preloadUtilityClasses()
        
        // 3. Ваши классы со сложной инициализацией
        preloadYourClasses()
    }
    
    private fun preloadSpringClasses() {
        arrayOf(
            "org.springframework.context.annotation.Configuration",
            "org.springframework.stereotype.Service",
            "org.springframework.transaction.support.TransactionTemplate"
        ).forEach { safePreload(it) }
    }
    
    private fun preloadUtilityClasses() {
        arrayOf(
            "java.util.concurrent.locks.ReentrantLock",
            "java.util.concurrent.ConcurrentHashMap",
            "com.fasterxml.jackson.databind.ObjectMapper"
        ).forEach { safePreload(it) }
    }
    
    private fun preloadYourClasses() {
        // Ваши классы с companion object init блоками
        arrayOf(
            "com.yourproject.config.DatabaseConfig",
            "com.yourproject.utils.ValidationUtils",
            "com.yourproject.services.*Service" // Паттерн
        ).forEach { safePreload(it) }
    }
    
    private fun safePreload(className: String) {
        try {
            if (className.endsWith(".*")) {
                preloadPackage(className.removeSuffix(".*"))
            } else {
                Class.forName(className)
            }
        } catch (e: Exception) {
            // Игнорируем - класс может не быть в classpath
        }
    }
    
    private fun preloadPackage(packageName: String) {
        // Упрощенная реализация - можно использовать Reflection
        println("Preloading package: $packageName")
    }
}
```

### **Шаг 2: Extension для автоматической предзагрузки**
```kotlin
class ClassPreloadingExtension : BeforeAllCallback {
    
    override fun beforeAll(context: ExtensionContext) {
        PreloadUtils.preloadCriticalClasses()
    }
}
```

### **Шаг 3: Использование в тестах**
```kotlin
// Способ 1: Аннотация на классе
@ExtendWith(ClassPreloadingExtension::class)
@Execution(ExecutionMode.CONCURRENT)
class MyConcurrentTest {
    // тесты
}

// Способ 2: Глобальная регистрация в junit-platform.properties
junit.jupiter.extensions.autodetection.enabled=true
```

## 🎯 **Ключевые моменты:**

1. **Используйте Extension** - он безопаснее для параллельного выполнения
2. **Kotlin uses companion object** вместо static блоков
3. **Предзагружайте** Spring components, locks, utilities
4. **Делайте предзагрузку один раз** на весь test run

## 📊 **Пример класса с "static" инициализацией в Kotlin:**
```kotlin
class DatabaseConfig {
    companion object {
        // Аналог static field
        val CONNECTION_POOL = createPool()
        
        // Аналог static block
        init {
            println("DatabaseConfig class loaded")
            initializeConnectionPool()
        }
        
        private fun createPool(): ConnectionPool {
            // Сложная инициализация
            return ConnectionPool()
        }
    }
}

// Предзагрузка такого класса
PreloadUtils.safePreload("com.yourproject.config.DatabaseConfig")
```

---

Отличный вопрос! Это **критически важная техника** для предотвращения дедлоков при параллельном запуске тестов. Давайте разберем детально.

## 🐛 **Проблема: Дедлоки на ClassLoader**

Когда JUnit 5 запускает тесты параллельно, несколько потоков одновременно пытаются:
1. **Загружать классы** через `ClassLoader`
2. **Инициализировать статические поля**
3. **Выполнять static-блоки**

ClassLoader internally синхронизирован, что приводит к дедлокам:

```
Found one Java-level deadlock:
=============================
"ForkJoinPool-1-worker-1":
  waiting to lock monitor 0x00007f405c003c20 (object 0x00000000e0b7e3c0, a jdk.internal.loader.ClassLoaders$AppClassLoader),
  which is held by "ForkJoinPool-1-worker-7"
```

## ✅ **Решение: Предзагрузка классов**

### **1. Базовый подход: Класс для предзагрузки**

```kotlin
package com.yourproject.testutils

import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.api.BeforeAll

/**
 * Предзагружает все классы, которые могут вызывать дедлоки
 */
object ClassPreloader {

    @JvmStatic
    @BeforeAll
    fun preloadCriticalClasses() {
        println("Preloading classes to avoid deadlocks...")
        
        // Предзагрузка Spring-компонентов
        preloadSpringClasses()
        
        // Предзагрузка утилитных классов
        preloadUtilityClasses()
        
        // Предзагрузка классов с static-инициализацией
        preloadStaticInitializers()
        
        println("Classes preloaded successfully")
    }

    private fun preloadSpringClasses() {
        arrayOf(
            "org.springframework.context.annotation.Configuration",
            "org.springframework.beans.factory.annotation.Autowired",
            "org.springframework.stereotype.Service",
            "org.springframework.stereotype.Component",
            "com.yourproject.configuration.AppConfig",
            "com.yourproject.services.*Service",
            "com.yourproject.repositories.*Repository"
        ).forEach { preloadClass(it) }
    }

    private fun preloadUtilityClasses() {
        arrayOf(
            "java.util.concurrent.locks.ReentrantLock",
            "java.util.concurrent.locks.ReentrantReadWriteLock",
            "java.util.Collections",
            "java.time.LocalDateTime",
            "com.fasterxml.jackson.databind.ObjectMapper"
        ).forEach { preloadClass(it) }
    }

    private fun preloadStaticInitializers() {
        // Классы со сложной static-инициализацией
        arrayOf(
            "org.slf4j.LoggerFactory",
            "ch.qos.logback.classic.Logger",
            "java.util.ResourceBundle",
            "java.text.SimpleDateFormat"
        ).forEach { preloadClass(it) }
    }

    private fun preloadClass(className: String) {
        try {
            if (className.endsWith(".*")) {
                // Загрузка по паттерну (упрощенная версия)
                val packageName = className.removeSuffix(".*")
                preloadPackageClasses(packageName)
            } else {
                Class.forName(className)
                println("Preloaded: $className")
            }
        } catch (e: ClassNotFoundException) {
            println("Warning: Class not found - $className")
        } catch (e: Exception) {
            println("Error preloading $className: ${e.message}")
        }
    }

    private fun preloadPackageClasses(packageName: String) {
        // Упрощенная реализация - в реальности нужен reflection для сканирования package
        println("Preloading package: $packageName.*")
    }
}
```

### **2. Использование в тестах**

```kotlin
import com.yourproject.testutils.ClassPreloader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.CONCURRENT)
class MyConcurrentTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            ClassPreloader.preloadCriticalClasses()
        }
    }

    @Test
    fun test1() {
        // Тест, который теперь безопасен
    }

    @Test 
    fun test2() {
        // Другой тест
    }
}
```

### **3. Автоматическая предзагрузка через Extension**

```kotlin
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class ClassPreloadingExtension : BeforeAllCallback {
    
    override fun beforeAll(context: ExtensionContext) {
        if (!classesPreloaded) {
            synchronized(ClassPreloadingExtension::class.java) {
                if (!classesPreloaded) {
                    PreloadUtils.preloadAllCriticalClasses()
                    classesPreloaded = true
                }
            }
        }
    }
    
    companion object {
        @Volatile
        private var classesPreloaded = false
    }
}

// Использование
@ExtendWith(ClassPreloadingExtension::class)
@Execution(ExecutionMode.CONCURRENT)
class MyConcurrentTests
```

### **4. Предзагрузка в junit-platform.properties**

```properties
# Предзагрузка перед всеми тестами
junit.platform.execution.listeners.include=com.yourproject.extensions.ClassPreloadingListener

# Параллельные настройки
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=4
```

## 🎯 **Ключевые классы для предзагрузки:**

### **Spring Framework:**
- `@Configuration`, `@Service`, `@Component` классы
- `ApplicationContext`, `BeanFactory`
- Все ваши бины и конфигурации

### **Утилитные классы:**
```kotlin
// Concurrent-классы
java.util.concurrent.locks.ReentrantLock::class.java
java.util.concurrent.ConcurrentHashMap::class.java

// Logging
org.slf4j.LoggerFactory::class.java
ch.qos.logback.classic.Logger::class.java

// JSON
com.fasterxml.jackson.databind.ObjectMapper::class.java
```

### **Ваши собственные классы:**
```kotlin
// Все классы со static-инициализацией
classOf<YourConfigClass>()
classOf<YourUtilityClass>()
classOf<YourServiceWithStaticBlock>()
```

## 🔧 **Как найти проблемные классы:**

### **1. Анализ дедлоков:**
Смотрите stack trace дедлока - какие классы участвуют в блокировке.

### **2. Профилирование:**
```bash
java -XX:+TraceClassLoading -jar your-tests.jar
```

### **3. Статический анализ:**
Ищите в коде:
- `static {}` блоки
- `synchronized` static методы
- Сложную инициализацию static полей

## 🚀 **Полный workflow предотвращения дедлоков:**

1. **Предзагрузка** всех критических классов в `@BeforeAll`
2. **Использование** `@Execution(ExecutionMode.CONCURRENT)`
3. **Мониторинг** работы ForkJoinPool
4. **Анализ** оставшихся проблем через thread dump


---
