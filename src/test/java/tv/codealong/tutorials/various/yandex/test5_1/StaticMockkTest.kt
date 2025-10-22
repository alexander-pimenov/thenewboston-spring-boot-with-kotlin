package tv.codealong.tutorials.various.yandex.test5_1

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

// src/test/kotlin/com/example/services/StaticMockkTest.kt

import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

/**
 * 🎯 ОСНОВНЫЕ ОТЛИЧИЯ MOCKK ОТ MOCKITO:
 * Mockito	Mockk	Преимущество Mockk
 * whenever(...).thenReturn(...)	every { ... } returns ...	Более Kotlin-идиоматично
 * verify(mock).method()	verify { mock.method() }	Более читаемый синтаксис
 * ArgumentCaptor	slot<T>() и capture(slot)	Проще и мощнее
 * argThat { }	match { }	Та же мощь, но Kotlin-стиль
 * @Mock    @MockK	Аналогично, но лучше интеграция
 * Нужны open классы	Мокает любые классы	Не требует изменений в production коде
 * 🚀 ПРЕИМУЩЕСТВА MOCKK:
 * 💯 Kotlin-first - идеально интегрируется с Kotlin
 *
 * 🎯 Простой синтаксис - every, verify, slot
 *
 * 🚀 Мощные возможности - мокинг статических методов, объектов, корутин
 *
 * 🔧 Гибкость - relaxed mocks, spies, capture slots
 *
 * 📚 Отличная документация - mockk.io
 */
class StaticMockkTest {

    @Test
    fun `should mock static methods and objects`() {
        // 🎯 Можем мокать даже статические методы!
        mockkStatic(UUID::class)

        val fixedUUID = UUID.fromString("12345678-1234-1234-1234-123456789abc")
        every { UUID.randomUUID() } returns fixedUUID

        // When - используем UUID в нашем коде
        val result = UUID.randomUUID()

        // Then
        assertEquals(fixedUUID, result)

        // Cleanup
        unmockkStatic(UUID::class)
    }

    @Test
    fun `should mock object declarations`() {
        // 🎯 Можем мокать object declarations
        mockkObject(GlobalConfig)

        every { GlobalConfig.maxLoginAttempts } returns 999
        every { GlobalConfig.isFeatureEnabled("new-ui") } returns true

        assertEquals(999, GlobalConfig.maxLoginAttempts)
        assertTrue(GlobalConfig.isFeatureEnabled("new-ui"))

        unmockkObject(GlobalConfig)
    }
}

// Пример object для тестирования
object GlobalConfig {
    const val maxLoginAttempts = 5
    fun isFeatureEnabled(feature: String): Boolean = false
}