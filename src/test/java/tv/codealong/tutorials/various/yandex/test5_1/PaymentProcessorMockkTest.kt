package tv.codealong.tutorials.various.yandex.test5_1

// src/test/kotlin/com/example/services/PaymentProcessorMockkTest.kt

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder

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
class PaymentProcessorMockkTest {

    @MockK
    lateinit var fraudDetectionService: FraudDetectionService

    @MockK
    lateinit var transactionRepository: TransactionRepository

    private lateinit var paymentProcessor: PaymentProcessor

    @BeforeEach
    fun setUp() {
        paymentProcessor = PaymentProcessor(fraudDetectionService, transactionRepository)
    }

    @Test
    fun `should process payment successfully when no fraud detected`() {
        // Given
        val userId = "user-123"
        val amount = 100.0

        every { fraudDetectionService.isSuspiciousTransaction(userId, amount) } returns false
        every { transactionRepository.save(any()) } answers { firstArg() }

        // When
        val result = paymentProcessor.processPayment(userId, amount, "Test payment")

        // Then
        assertTrue(result is PaymentResult.Success)

        // 🎯 Проверяем количество вызовов
        verify(exactly = 1) { fraudDetectionService.isSuspiciousTransaction(userId, amount) }
        verify(exactly = 2) { transactionRepository.save(any()) } // PENDING и COMPLETED
    }

    @Test
    fun `should detect fraud and reject payment`() {
        // Given
        val userId = "fraud-user"
        val amount = 10000.0

        every { fraudDetectionService.isSuspiciousTransaction(userId, amount) } returns true

        // When
        val result = paymentProcessor.processPayment(userId, amount, "Suspicious payment")

        // Then
        assertTrue(result is PaymentResult.FraudDetected)

        // 🎯 Проверяем что НЕ сохраняли транзакцию
        verify(exactly = 0) { transactionRepository.save(any()) }
    }

    @Test
    fun `should save transaction with correct data using slots`() {
        // Given
        val userId = "user-123"
        val amount = 50.0
        val description = "Test transaction"

        every { fraudDetectionService.isSuspiciousTransaction(any(), any()) } returns false

        // 🎯 Используем slot для захвата аргументов
        val pendingTransactionSlot = slot<Transaction>()
        val completedTransactionSlot = slot<Transaction>()

        every { transactionRepository.save(capture(pendingTransactionSlot)) } answers { firstArg() }
        every { transactionRepository.save(capture(completedTransactionSlot)) } answers { firstArg() }

        // When
        paymentProcessor.processPayment(userId, amount, description)

        // Then - проверяем capture'рованные транзакции
        assertEquals(TransactionStatus.PENDING, pendingTransactionSlot.captured.status)
        assertEquals(userId, pendingTransactionSlot.captured.userId)
        assertEquals(amount, pendingTransactionSlot.captured.amount)

        assertEquals(TransactionStatus.COMPLETED, completedTransactionSlot.captured.status)
        assertEquals(pendingTransactionSlot.captured.id, completedTransactionSlot.captured.id)
    }

    @Test
    fun `should verify method call order`() {
        // Given
        every { fraudDetectionService.isSuspiciousTransaction(any(), any()) } returns false
        every { transactionRepository.save(any()) } answers { firstArg() }

        // 🎯 Проверяем порядок вызовов
        val order = inOrder(fraudDetectionService, transactionRepository)

        // When
        paymentProcessor.processPayment("user-123", 100.0, "Order test")

        // Then
        order.verify { fraudDetectionService.isSuspiciousTransaction(any(), any()) }
        order.verify(1) { transactionRepository.save(any()) }
    }
}