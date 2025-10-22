package tv.codealong.tutorials.various.yandex.test5_1

// src/test/kotlin/com/example/services/UserServiceTest.kt

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDateTime

/**
 * 🎯 КЛЮЧЕВЫЕ ФИЧИ MOCKITO В ТЕСТАХ:
 * whenever(...).thenReturn(...) - настройка возвращаемых значений
 *
 * verify(mock).method() - проверка вызовов методов
 *
 * argThat { } - проверка аргументов с предикатом
 *
 * never(), times(n), atLeast(n) - проверка количества вызовов
 *
 * inOrder.verify() - проверка порядка вызовов
 *
 * argumentCaptor - захват аргументов для детальной проверки
 *
 * eq(), any() - матчеры для аргументов
 */
class UserServiceTest {

    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepository
    private lateinit var emailService: EmailService

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        emailService = mock()
        userService = UserService(userRepository, emailService)
    }

    @Test
    fun `should register user successfully when email is not taken`() {
        // Given - настраиваем моки
        val email = "test@example.com"
        val name = "Test User"

        // Когда ищут пользователя по email - возвращаем null (не найден)
        whenever(userRepository.findByEmail(email)).thenReturn(null)

        // Когда сохраняют пользователя - возвращаем его же
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        // Когда отправляют email - возвращаем true
        whenever(emailService.sendWelcomeEmail(any())).thenReturn(true)

        // When
        val result = userService.registerUser(email, name)

        // Then
        assertEquals(email, result.email)
        assertEquals(name, result.name)
        assertTrue(result.isActive)

        // Verify - проверяем что методы были вызваны
        verify(userRepository).findByEmail(email)
        verify(userRepository).save(any())
        verify(emailService).sendWelcomeEmail(any())

        // Verify что sendWelcomeEmail был вызван с правильным пользователем
        verify(emailService).sendWelcomeEmail(
            argThat {
                this.email == email && this.name == name
            }
        )
    }

    @Test
    fun `should throw exception when registering with existing email`() {
        // Given
        val email = "existing@example.com"
        val name = "Existing User"
        val existingUser = User("123", email, "Old User")

        whenever(userRepository.findByEmail(email)).thenReturn(existingUser)

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            userService.registerUser(email, name)
        }

        assertEquals("User with email $email already exists", exception.message)

        // Verify что save НЕ был вызван
        verify(userRepository, never()).save(any())
        verify(emailService, never()).sendWelcomeEmail(any())
    }

    @Test
    fun `should deactivate user when user exists`() {
        // Given
        val userId = "user-123"
        val activeUser = User(userId, "test@example.com", "Test User", isActive = true)
        val deactivatedUser = activeUser.copy(isActive = false)

        whenever(userRepository.findById(userId)).thenReturn(activeUser)
        whenever(userRepository.save(deactivatedUser)).thenReturn(deactivatedUser)

        // When
        val result = userService.deactivateUser(userId)

        // Then
        assertTrue(result)

        // Verify что сохранили деактивированного пользователя
        verify(userRepository).save(
            argThat { !this.isActive }
        )
    }

    @Test
    fun `should return false when deactivating non-existent user`() {
        // Given
        val userId = "non-existent"
        whenever(userRepository.findById(userId)).thenReturn(null)

        // When
        val result = userService.deactivateUser(userId)

        // Then
        assertFalse(result)
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `should send password reset email for existing user`() {
        // Given
        val email = "user@example.com"
        val user = User("123", email, "Test User")

        whenever(userRepository.findByEmail(email)).thenReturn(user)
        whenever(emailService.sendPasswordResetEmail(email)).thenReturn(true)

        // When
        val result = userService.requestPasswordReset(email)

        // Then
        assertTrue(result)
        verify(emailService).sendPasswordResetEmail(email)
    }

    @Test
    fun `should return false when sending password reset for non-existent user`() {
        // Given
        val email = "nonexistent@example.com"
        whenever(userRepository.findByEmail(email)).thenReturn(null)

        // When
        val result = userService.requestPasswordReset(email)

        // Then
        assertFalse(result)
        verify(emailService, never()).sendPasswordResetEmail(any())
    }

    @Test
    fun `should handle email service failure gracefully`() {
        // Given
        val email = "test@example.com"
        val name = "Test User"

        whenever(userRepository.findByEmail(email)).thenReturn(null)
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }
        whenever(emailService.sendWelcomeEmail(any())).thenReturn(false) // Email service failed

        // When - регистрация должна пройти успешно даже если email не отправился
        val result = userService.registerUser(email, name)

        // Then
        assertEquals(email, result.email)

        // Verify что методы всё равно были вызваны
        verify(userRepository).findByEmail(email)
        verify(userRepository).save(any())
        verify(emailService).sendWelcomeEmail(any())
    }
}