package tv.codealong.tutorials.various.yandex.test5_1

// src/test/kotlin/com/example/services/PaymentProcessorTest.kt

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import java.util.*

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
class PaymentProcessorTest {

    private lateinit var paymentProcessor: PaymentProcessor
    private lateinit var fraudDetectionService: FraudDetectionService
    private lateinit var transactionRepository: TransactionRepository

    @BeforeEach
    fun setUp() {
        fraudDetectionService = mock()
        transactionRepository = mock()
        paymentProcessor = PaymentProcessor(fraudDetectionService, transactionRepository)
    }

    @Test
    fun `should process payment successfully when no fraud detected`() {
        // Given
        val userId = "user-123"
        val amount = 100.0
        val description = "Test payment"

        whenever(fraudDetectionService.isSuspiciousTransaction(userId, amount)).thenReturn(false)
        whenever(transactionRepository.save(any())).thenAnswer { it.arguments[0] as Transaction }

        // When
        val result = paymentProcessor.processPayment(userId, amount, description)

        // Then
        assertTrue(result is PaymentResult.Success)

        // Verify что проверили на мошенничество
        verify(fraudDetectionService).isSuspiciousTransaction(userId, amount)

        // Verify что сохранили транзакцию дважды (PENDING и COMPLETED)
        verify(transactionRepository, times(2)).save(any())
    }

    @Test
    fun `should detect fraud and reject payment`() {
        // Given
        val userId = "user-123"
        val amount = 10000.0 // Большая сумма
        val description = "Suspicious payment"

        whenever(fraudDetectionService.isSuspiciousTransaction(userId, amount)).thenReturn(true)

        // When
        val result = paymentProcessor.processPayment(userId, amount, description)

        // Then
        assertTrue(result is PaymentResult.FraudDetected)

        // Verify что НЕ сохраняли транзакцию
        verify(transactionRepository, never()).save(any())
    }

    @Test
    fun `should save transaction with correct data`() {
        // Given
        val userId = "user-123"
        val amount = 50.0
        val description = "Test transaction"

        whenever(fraudDetectionService.isSuspiciousTransaction(userId, amount)).thenReturn(false)
        whenever(transactionRepository.save(any())).thenAnswer { it.arguments[0] as Transaction }

        // When
        val result = paymentProcessor.processPayment(userId, amount, description)

        // Then
        // Verify что сохранили транзакцию с правильными данными
        verify(transactionRepository).save(
            argThat {
                this.userId == userId &&
                        this.amount == amount &&
                        this.description == description &&
                        this.status == TransactionStatus.PENDING
            }
        )

        // И потом с COMPLETED статусом
        verify(transactionRepository).save(
            argThat {
                this.status == TransactionStatus.COMPLETED
            }
        )
    }

    @Test
    fun `should use different transaction ids for different payments`() {
        // Given
        whenever(fraudDetectionService.isSuspiciousTransaction(any(), any())).thenReturn(false)

        val capturedTransactions = mutableListOf<Transaction>()
        whenever(transactionRepository.save(any())).thenAnswer {
            val transaction = it.arguments[0] as Transaction
            capturedTransactions.add(transaction)
            transaction
        }

        // When - обрабатываем два платежа
        paymentProcessor.processPayment("user1", 10.0, "Payment 1")
        paymentProcessor.processPayment("user2", 20.0, "Payment 2")

        // Then - у транзакций должны быть разные ID
        assertEquals(4, capturedTransactions.size) // 2 платежа × 2 сохранения каждый
        val transactionIds = capturedTransactions.map { it.id }.toSet()
        assertEquals(4, transactionIds.size) // Все ID должны быть уникальными
    }
}