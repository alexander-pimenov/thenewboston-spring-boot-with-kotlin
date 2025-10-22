package tv.codealong.tutorials.various.yandex.test5_1

// src/test/kotlin/com/example/services/UserServiceMockkTest.kt

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

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
class UserServiceMockkTest {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var emailService: EmailService

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository, emailService)
    }

    @Test
    fun `should register user successfully when email is not taken`() {
        // Given - настраиваем моки
        val email = "test@example.com"
        val name = "Test User"

        // 🎯 Mockk стиль - более читаемо!
        every { userRepository.findByEmail(email) } returns null
        every { userRepository.save(any()) } answers { firstArg() } // возвращаем первый аргумент
        every { emailService.sendWelcomeEmail(any()) } returns true

        // When
        val result = userService.registerUser(email, name)

        // Then
        assertEquals(email, result.email)
        assertEquals(name, result.name)
        assertTrue(result.isActive)

        // 🎯 Mockk verify - более гибкий!
        verify {
            userRepository.findByEmail(email)
            userRepository.save(any())
            emailService.sendWelcomeEmail(any())
        }

        // Более точная проверка аргументов
        verify {
            emailService.sendWelcomeEmail(
                match { user ->
                    user.email == email && user.name == name
                }
            )
        }
    }

    @Test
    fun `should throw exception when registering with existing email`() {
        // Given
        val email = "existing@example.com"
        val existingUser = User("123", email, "Old User")

        every { userRepository.findByEmail(email) } returns existingUser

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            userService.registerUser(email, "New User")
        }

        assertEquals("User with email $email already exists", exception.message)

        // 🎯 Проверяем что методы НЕ вызывались
        verify(exactly = 0) {
            userRepository.save(any())
            emailService.sendWelcomeEmail(any())
        }

        confirmVerified(userRepository, emailService) // проверяем что нет лишних вызовов
    }

    @Test
    fun `should deactivate user when user exists`() {
        // Given
        val userId = "user-123"
        val activeUser = User(userId, "test@example.com", "Test User", isActive = true)

        every { userRepository.findById(userId) } returns activeUser
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val result = userService.deactivateUser(userId)

        // Then
        assertTrue(result)

        // 🎯 Проверяем что сохранили деактивированного пользователя
        verify {
            userRepository.save(
                match { user -> !user.isActive }
            )
        }
    }

    @Test
    fun `should capture arguments for detailed verification`() {
        // Given
        val email = "capture@example.com"
        val name = "Captured User"

        every { userRepository.findByEmail(any()) } returns null
        every { userRepository.save(any()) } answers { firstArg() }
        every { emailService.sendWelcomeEmail(any()) } returns true

        // 🎯 Capture аргументов
        val savedUserSlot = slot<User>()
        val emailSlot = slot<String>()

        // When
        userService.registerUser(email, name)

        // Then - проверяем capture'рованные значения
        verify {
            userRepository.findByEmail(capture(emailSlot))
            userRepository.save(capture(savedUserSlot))
        }

        assertEquals(email, emailSlot.captured)
        assertEquals(name, savedUserSlot.captured.name)
        assertEquals(email, savedUserSlot.captured.email)
    }

    @Test
    fun `should handle multiple users with different responses`() {
        // Given
        val user1 = User("1", "user1@test.com", "User One")
        val user2 = User("2", "user2@test.com", "User Two")

        // 🎯 Разные ответы для разных аргументов
        every { userRepository.findByEmail("user1@test.com") } returns null
        every { userRepository.findByEmail("user2@test.com") } returns user2
        every { userRepository.save(any()) } answers { firstArg() }
        every { emailService.sendWelcomeEmail(any()) } returns true

        // When & Then - первый пользователь должен зарегистрироваться
        val result1 = userService.registerUser("user1@test.com", "User One")
        assertNotNull(result1)

        // Когда & Тогда - второй должен выбросить исключение
        assertThrows<IllegalArgumentException> {
            userService.registerUser("user2@test.com", "User Two")
        }

        // Проверяем вызовы
        verify(exactly = 1) { userRepository.save(any()) } // только для первого пользователя
    }
}