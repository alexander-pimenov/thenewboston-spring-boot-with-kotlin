package tv.codealong.tutorials.various.yandex.test5_1

// src/test/kotlin/com/example/services/AdvancedMockitoTest.kt

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.argumentCaptor

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
class AdvancedMockitoTest {

    @Test
    fun `should capture arguments for verification`() {
        // Given
        val userRepository: UserRepository = mock()
        val emailService: EmailService = mock()
        val userService = UserService(userRepository, emailService)

        val emailCaptor = argumentCaptor<String>()
        val userCaptor = argumentCaptor<User>()

        whenever(userRepository.findByEmail(any())).thenReturn(null)
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }
        whenever(emailService.sendWelcomeEmail(any())).thenReturn(true)

        // When
        userService.registerUser("captured@example.com", "Captured User")

        // Then - используем captor для проверки аргументов
        verify(userRepository).findByEmail(emailCaptor.capture())
        assertEquals("captured@example.com", emailCaptor.firstValue)

        verify(emailService).sendWelcomeEmail(userCaptor.capture())
        assertEquals("captured@example.com", userCaptor.firstValue.email)
        assertEquals("Captured User", userCaptor.firstValue.name)
    }

    @Test
    fun `should verify method call order`() {
        // Given
        val userRepository: UserRepository = mock()
        val emailService: EmailService = mock()
        val userService = UserService(userRepository, emailService)

        whenever(userRepository.findByEmail(any())).thenReturn(null)
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }
        whenever(emailService.sendWelcomeEmail(any())).thenReturn(true)

        val inOrder = inOrder(userRepository, emailService)

        // When
        userService.registerUser("test@example.com", "Test User")

        // Then - проверяем порядок вызовов
        inOrder.verify(userRepository).findByEmail("test@example.com")
        inOrder.verify(userRepository).save(any())
        inOrder.verify(emailService).sendWelcomeEmail(any())
    }

    @Test
    fun `should handle multiple calls with different responses`() {
        // Given
        val fraudDetectionService: FraudDetectionService = mock()
        val transactionRepository: TransactionRepository = mock()
        val paymentProcessor = PaymentProcessor(fraudDetectionService, transactionRepository)

        // Настраиваем разные ответы для разных вызовов
        whenever(fraudDetectionService.isSuspiciousTransaction(eq("safe-user"), any()))
            .thenReturn(false)
        whenever(fraudDetectionService.isSuspiciousTransaction(eq("fraud-user"), any()))
            .thenReturn(true)

        whenever(transactionRepository.save(any())).thenAnswer { it.arguments[0] as Transaction }

        // When
        val safeResult = paymentProcessor.processPayment("safe-user", 100.0, "Safe payment")
        val fraudResult = paymentProcessor.processPayment("fraud-user", 1000.0, "Fraud payment")

        // Then
        assertTrue(safeResult is PaymentResult.Success)
        assertTrue(fraudResult is PaymentResult.FraudDetected)

        // Verify что для safe-user сохранили транзакцию, для fraud-user - нет
        verify(transactionRepository, times(2)).save(any()) // Только для safe-user (2 раза)
    }
}