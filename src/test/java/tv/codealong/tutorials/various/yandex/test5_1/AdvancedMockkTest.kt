package tv.codealong.tutorials.various.yandex.test5_1

// src/test/kotlin/com/example/services/AdvancedMockkTest.kt

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import kotlinx.coroutines.runBlocking

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
@ExtendWith(MockKExtension::class)
class AdvancedMockkTest {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var emailService: EmailService

    @Test
    fun `should use relaxed mocks for optional dependencies`() {
        // 🎯 Relaxed mock - не требует настройки всех методов
        val relaxedRepository = mockk<UserRepository>(relaxed = true)
        val relaxedEmailService = mockk<EmailService>(relaxed = true)

        val userService = UserService(relaxedRepository, relaxedEmailService)

        // Можно вызывать без every {} для всех методов
        assertDoesNotThrow {
            userService.registerUser("test@example.com", "Test User")
        }
    }

    @Test
    fun `should mock object with justRun and justAwait`() {
        // Given
        val userService = UserService(userRepository, emailService)

        // 🎯 justRun - для void методов
        justRun { userRepository.delete(any()) }

        // When & Then
        assertDoesNotThrow {
            // Предположим что у нас есть метод delete
            // userService.deleteUser("123")
        }
    }

    @Test
    fun `should verify with timeout for async operations`() {
        // Given
        val userService = UserService(userRepository, emailService)

        every { userRepository.findByEmail(any()) } returns null
        every { userRepository.save(any()) } answers { firstArg() }
        every { emailService.sendWelcomeEmail(any()) } returns true

        // When
        userService.registerUser("async@example.com", "Async User")

        // 🎯 Verify с таймаутом (полезно для асинхронных операций)
        verify(timeout = 1000) {
            emailService.sendWelcomeEmail(any())
        }
    }

    @Test
    fun `should use spy for partial mocking`() {
        // 🎯 Spy - реальный объект с возможностью мокинга отдельных методов
        val realUserService = UserService(userRepository, emailService)
        val userServiceSpy = spyk(realUserService)

        every { userRepository.findByEmail(any()) } returns null
        every { userRepository.save(any()) } answers {
            firstArg<User>().copy(id = "mocked-id")
        }
        every { emailService.sendWelcomeEmail(any()) } returns true

        // Можем замокать только конкретный метод spy
        every { userServiceSpy.deactivateUser(any()) } returns false

        // When
        val registrationResult = userServiceSpy.registerUser("spy@example.com", "Spy User")
        val deactivationResult = userServiceSpy.deactivateUser("123")

        // Then
        assertEquals("mocked-id", registrationResult.id)
        assertFalse(deactivationResult) // замоканный метод

        verify { userServiceSpy.deactivateUser(any()) }
    }

    @Test
    fun `should mock coroutines with coEvery and coVerify`() = runBlocking {
        // 🎯 Для корутин используем coEvery и coVerify
        val asyncService = mockk<AsyncEmailService>()

        coEvery { asyncService.sendAsyncWelcomeEmail(any()) } returns true

        // Тестируем асинхронный код...

        coVerify { asyncService.sendAsyncWelcomeEmail(any()) }
    }
}

// Пример асинхронного сервиса для теста
interface AsyncEmailService {
    suspend fun sendAsyncWelcomeEmail(user: User): Boolean
}