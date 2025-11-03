package tv.codealong.tutorials.various.yandex.test6_2_real_from_yndx_anddeepseek

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.math.BigDecimal
import java.time.Instant

class LimitCheckServiceTest {

    private val userLimitsProvider: UserLimitsProvider = mockk()
    private val paymentHistoryProvider: PaymentHistoryProvider = mockk()
    private val limitCheckService = LimitCheckService(userLimitsProvider, paymentHistoryProvider)

    private val testUserId = "user-123"
    private val testTime = Instant.parse("2024-01-15T12:00:00Z")

    private val defaultLimits = UserLimits(
        userId = testUserId,
        dailyLimit = BigDecimal("50000.00"),        // 50,000 руб
        maxSingleOperation = BigDecimal("15000.00") // 15,000 руб
    )

    @BeforeEach
    fun setUp() {
        every { userLimitsProvider.getLimits(testUserId) } returns defaultLimits
    }

    @Nested
    @DisplayName("Limits checks for user with no limits")
    inner class WhenUserHasNoLimits {

        @BeforeEach
        fun setUp() {
            every { userLimitsProvider.getLimits(testUserId) } returns null
        }

        @Test
        fun `should allow any payment`() {
            val payment = Payment(
                userId = testUserId,
                amount = BigDecimal("100000.00"),
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            val result = limitCheckService.checkPaymentLimits(payment)

            assertTrue(result.isAllowed)
            assertNull(result.rejectionReason)
        }
    }

    @Nested
    @DisplayName("Single operation limit checks")
    inner class SingleOperationLimitChecks {

        @Test
        fun `should reject when payment exceeds single operation limit`() {
            val payment = Payment(
                userId = testUserId,
                amount = BigDecimal("16000.00"), // превышает 15,000
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            every {
                paymentHistoryProvider.getUserPayments(
                    testUserId,
                    testTime.minus(24, java.time.temporal.ChronoUnit.HOURS),
                    testTime
                )
            } returns emptyList()

            val result = limitCheckService.checkPaymentLimits(payment)

            assertFalse(result.isAllowed)
            assertEquals(RejectionReason.SINGLE_OPERATION_LIMIT_EXCEEDED, result.rejectionReason)
        }

        @Test
        fun `should allow when payment equals single operation limit`() {
            val payment = Payment(
                userId = testUserId,
                amount = BigDecimal("15000.00"), // равно лимиту
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            every {
                paymentHistoryProvider.getUserPayments(any(), any(), any())
            } returns emptyList()

            val result = limitCheckService.checkPaymentLimits(payment)

            assertTrue(result.isAllowed)
            assertNull(result.rejectionReason)
        }

        @Test
        fun `should allow when payment below single operation limit`() {
            val payment = Payment(
                userId = testUserId,
                amount = BigDecimal("14999.99"), // ниже лимита
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            every {
                paymentHistoryProvider.getUserPayments(any(), any(), any())
            } returns emptyList()

            val result = limitCheckService.checkPaymentLimits(payment)

            assertTrue(result.isAllowed)
            assertNull(result.rejectionReason)
        }
    }

    @Nested
    @DisplayName("Daily limit checks")
    inner class DailyLimitChecks {

        @Test
        fun `should reject when payment exceeds daily limit`() {
            val existingPayments = listOf(
                Payment(testUserId, BigDecimal("45000.00"), OperationType.DEBIT, testTime.minus(1, java.time.temporal.ChronoUnit.HOURS))
            )

            val newPayment = Payment(
                userId = testUserId,
                amount = BigDecimal("6000.00"), // 45000 + 6000 = 51000 > 50000
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            every {
                paymentHistoryProvider.getUserPayments(
                    testUserId,
                    testTime.minus(24, java.time.temporal.ChronoUnit.HOURS),
                    testTime
                )
            } returns existingPayments

            val result = limitCheckService.checkPaymentLimits(newPayment)

            assertFalse(result.isAllowed)
            assertEquals(RejectionReason.DAILY_LIMIT_EXCEEDED, result.rejectionReason)
        }

        @Test
        fun `should allow when payment with existing payments stays within daily limit`() {
            val existingPayments = listOf(
                Payment(testUserId, BigDecimal("30000.00"), OperationType.DEBIT, testTime.minus(2, java.time.temporal.ChronoUnit.HOURS)),
                Payment(testUserId, BigDecimal("10000.00"), OperationType.DEBIT, testTime.minus(1, java.time.temporal.ChronoUnit.HOURS))
            )

            val newPayment = Payment(
                userId = testUserId,
                amount = BigDecimal("9999.99"), // 30000 + 10000 + 9999.99 = 49999.99 < 50000
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            every {
                paymentHistoryProvider.getUserPayments(
                    testUserId,
                    testTime.minus(24, java.time.temporal.ChronoUnit.HOURS),
                    testTime
                )
            } returns existingPayments

            val result = limitCheckService.checkPaymentLimits(newPayment)

            assertTrue(result.isAllowed)
            assertNull(result.rejectionReason)
        }

        @Test
        @DisplayName("следует учитывать только платежи, произведенные в течение последних 24 часов")
        fun `should consider only payments within last 24 hours`() {
            //этот платеж просто для примера, в список за последние 24 часа он не добавлен.
            val oldPayment1 = Payment(
                testUserId,
                BigDecimal("15000.00"),
                OperationType.DEBIT,
                testTime.minus(25, java.time.temporal.ChronoUnit.HOURS) // 25 часов назад - не должно учитываться
            )

            val oldPayment2 = Payment(
                testUserId,
                BigDecimal("9000.00"),
                OperationType.DEBIT,
                testTime.minus(23, java.time.temporal.ChronoUnit.HOURS) // 23 часов назад
            )

            val oldPayment3 = Payment(
                testUserId,
                BigDecimal("6000.00"),
                OperationType.DEBIT,
                testTime.minus(20, java.time.temporal.ChronoUnit.HOURS) // 20 часов назад
            )

            val oldPayment4 = Payment(
                testUserId,
                BigDecimal("12000.00"),
                OperationType.DEBIT,
                testTime.minus(13, java.time.temporal.ChronoUnit.HOURS) // 13 часов назад
            )

            val recentPayment = Payment(
                testUserId,
                BigDecimal("1000.00"),
                OperationType.DEBIT,
                testTime.minus(5, java.time.temporal.ChronoUnit.HOURS) // 5 часов назад - должно учитываться
            )

            val existingPayments = listOf(oldPayment2, oldPayment3, oldPayment4, recentPayment)

            val newPayment = Payment(
                userId = testUserId,
                amount = BigDecimal("12800.00"), // 128000 + 37000 = 49800 (в пределах лимита)
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            every {
                paymentHistoryProvider.getUserPayments(
                    testUserId,
                    testTime.minus(24, java.time.temporal.ChronoUnit.HOURS),
                    testTime
                )
            } returns existingPayments // oldPayment не попадает в период

            val result = limitCheckService.checkPaymentLimits(newPayment)

            assertTrue(result.isAllowed)
            assertNull(result.rejectionReason)
        }

        @Test
        fun `should allow when total equals daily limit`() {
            val existingPayments = listOf(
                Payment(testUserId, BigDecimal("50000.00"), OperationType.DEBIT, testTime.minus(1, java.time.temporal.ChronoUnit.HOURS))
            )

            val newPayment = Payment(
                userId = testUserId,
                amount = BigDecimal("0.00"), // 50000 + 0 = 50000 (равно лимиту)
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            every {
                paymentHistoryProvider.getUserPayments(any(), any(), any())
            } returns existingPayments

            val result = limitCheckService.checkPaymentLimits(newPayment)

            assertTrue(result.isAllowed)
            assertNull(result.rejectionReason)
        }
    }

    @Nested
    @DisplayName("Edge cases (Крайние случаи)")
    inner class EdgeCases {

        @Test
        fun `should handle zero amount payment`() {
            val payment = Payment(
                userId = testUserId,
                amount = BigDecimal.ZERO,
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            every {
                paymentHistoryProvider.getUserPayments(any(), any(), any())
            } returns emptyList()

            val result = limitCheckService.checkPaymentLimits(payment)

            assertTrue(result.isAllowed)
            assertNull(result.rejectionReason)
        }

        @Test
        fun `should handle very small amount`() {
            val payment = Payment(
                userId = testUserId,
                amount = BigDecimal("0.01"),
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            every {
                paymentHistoryProvider.getUserPayments(any(), any(), any())
            } returns emptyList()

            val result = limitCheckService.checkPaymentLimits(payment)

            assertTrue(result.isAllowed)
            assertNull(result.rejectionReason)
        }
    }

    @Nested
    @DisplayName("Integration scenarios")
    inner class IntegrationScenarios {

        @Test
        fun `should check both limits and fail on single operation first`() {
            val payment = Payment(
                userId = testUserId,
                amount = BigDecimal("20000.00"), // превышает single operation limit
                operationType = OperationType.DEBIT,
                operationTime = testTime
            )

            // Даже если daily limit тоже будет превышен, сначала проверяется single operation
            val existingPayments = listOf(
                Payment(testUserId, BigDecimal("49000.00"), OperationType.DEBIT, testTime.minus(1, java.time.temporal.ChronoUnit.HOURS))
            )

            every {
                paymentHistoryProvider.getUserPayments(any(), any(), any())
            } returns existingPayments

            val result = limitCheckService.checkPaymentLimits(payment)

            assertFalse(result.isAllowed)
            assertEquals(RejectionReason.SINGLE_OPERATION_LIMIT_EXCEEDED, result.rejectionReason)
        }
    }
}