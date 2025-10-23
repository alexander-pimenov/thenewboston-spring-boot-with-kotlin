package tv.codealong.tutorials.various.yandex.test6_1_real_from_yndx

import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class LimitServiceTest {

    private lateinit var limitService: LimitService

    @MockK(relaxed = true)
    private lateinit var repo: LimitRepo

    @MockK(relaxed = true)
    private lateinit var historyOperation: HistoryOperation

    @BeforeEach
    fun setUp() {
        limitService = LimitServiceImpl(repo, historyOperation)
    }

    @Test
    fun `should success limit`() {

        val dayLimit = DayLimit(BigDecimal(100), BigDecimal(50))
        val listPayments = listOf(
            Payment("1", BigDecimal(5), TypeOperation.DEBIT, LocalDateTime.of(2025, 10, 21, 1, 10)),
            Payment("1", BigDecimal(15), TypeOperation.DEBIT, LocalDateTime.of(2025, 10, 21, 1, 35)),
            Payment("1", BigDecimal(25), TypeOperation.DEBIT, LocalDateTime.of(2025, 10, 21, 2, 45)),
            Payment("1", BigDecimal(3), TypeOperation.DEBIT, LocalDateTime.of(2025, 10, 21, 3, 15)),
            Payment("1", BigDecimal(7), TypeOperation.DEBIT, LocalDateTime.of(2025, 10, 21, 5, 25))
        )
        //можно изменять сумму платежа, и наблюдать за тестом
        val payment = Payment("1", BigDecimal(55), TypeOperation.DEBIT, LocalDateTime.of(2025, 10, 21, 10, 10))


        every { repo.getDayLimit(any()) } returns dayLimit
        every {
            historyOperation.getHistoryOperation(
                any(),
                TypeOperation.DEBIT,
                LocalDateTime.of(2025, 10, 21, 0, 0),
                LocalDateTime.of(2025, 10, 21, 23, 59)
            )
        } returns listPayments

        val result = limitService.checkLimit(payment)

        when (result) {
            is CheckStatusLimit.Approved -> {
                println("Approved")
                assertTrue(actual = result == CheckStatusLimit.Approved)
            }
            is CheckStatusLimit.Reject -> {
                println("Rejected: ${result.description}")
                assertTrue(actual = result.description == "limit.maxAmountPerOperation")
            }
        }
    }

}




