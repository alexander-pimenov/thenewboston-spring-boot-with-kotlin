package tv.codealong.tutorials.various.countDownLatch

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.http.ResponseEntity
import tv.codealong.tutorials.various.audit.AuditEventMessage
import tv.codealong.tutorials.various.audit.AuditSender
import java.time.LocalDateTime
import java.util.*

class WaitingSendEventTest {

    private val validatorHelper: Any = Any()

    @Test
    fun `waitingSendEvent should complete immediately when latch is already zero`() {
        val latch = CountDownLatch(0)
        assertDoesNotThrow { waitingSendEvent(latch) }
    }

    @Test
    fun `waitingSendEvent should complete when latch counts down within timeout`() = runBlocking {
        val latch = CountDownLatch(1)

        launch {
            delay(100)
            latch.countDown()
        }

        assertDoesNotThrow { waitingSendEvent(latch) }
    }

    @Test
    @Timeout(5) // ограничиваем время выполнения теста
    fun `waitingSendEvent should throw exception when timeout exceeded`() {
        val latch = CountDownLatch(1)

        val exception = assertThrows<AssertionError> {
            waitingSendEvent(latch)
        }

        assertTrue(exception.message!!.contains("sendEvent не был вызван за 30 секунд"))
    }

    @Test
    fun `waitingSendEvent should have correct error message on timeout`() {
        val latch = CountDownLatch(1)

        val exception = assertThrows<AssertionError> {
            waitingSendEvent(latch)
        }

        assertEquals("sendEvent не был вызван за 30 секунд", exception.message)
    }

    @Test
    fun `waitingSendEvent should work with mock scenario`() = runBlocking {
        val latch = CountDownLatch(1)
        val sender = mock<AuditSender> {
            on { sendEvent(any<AuditEventMessage>()) } doAnswer {
                latch.countDown()
            }
        }

        launch {
            delay(50)
            sender.sendEvent(mock())
        }

        assertDoesNotThrow { waitingSendEvent(latch) }
    }

    @Test
    fun `when invoke event dictionary should invoke saver`() {
        /* Given */
        val sender = initAuditSaver()
        val uuid = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        val response = ResponseEntity.ok(
            Dictionary(
                id = uuid,
                code = "123",
                tenantId = uuid,
                rowSchema = {},
                constraints = Constraint(listOf("name")),
                updatedAt = now
            )
        )

        /* When */
        runTest {
            runAuditable(
                operationName = DICTIONARY_CREATE,
                lambda = { response }
            )
        }

        /* Then */
        verify(sender).sendEvent(any<AuditEventMessage>())
    }

    /**
     * Ожидает вызова sendEvent и может использоваться для тестирования асинхронных операций.
     * Ожидает до тех пор, пока latch не станет равным нулю или не истечет указанное время ожидания.
     */
    fun waitingSendEvent(latch: CountDownLatch) {
        val eventCalled = latch.await(30, TimeUnit.SECONDS)
        Assertions.assertTrue(eventCalled, "sendEvent не был вызван за 30 секунд")
    }

    fun initAuditSaver(): AuditSender {
        val sender = mock<AuditSender> {
            on { sendEvent(any<AuditEventMessage>()) } doAnswer {}
            on { sendMeta() } doAnswer {}
        }
        AuditSaverSupport(
            validatorHelper = validatorHelper,
            auditSenders = listOf(sender)
        ).also {
            it.sendMetadata()
        }
        return sender
    }

    companion object {
        const val DICTIONARY_CREATE = "DICTIONARY_CREATE"
    }
}

