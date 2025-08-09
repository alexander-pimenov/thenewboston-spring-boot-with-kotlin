package tv.codealong.tutorials.various.audit

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BalancingGroupCheckerTest {

    @Test
    fun `should return true if balancing group is NONE`() {
        val checker = BalancingGroupChecker()
        val cb = CircuitBreakerConfiguration()
        val configs = listOf(
            BalancingGroupConfiguration("HttpSender group", false, "com.transport.http.HttpSender", cb, 20),
            BalancingGroupConfiguration("KafkaBuffer group", false,  "com.transport.buffer.kafka.KafkaBuffer", cb, -100),
            BalancingGroupConfiguration("KafkaSender group", true, "com.transport.kafka.KafkaSender", cb, 30),
            BalancingGroupConfiguration("HttpSender group", false, "com.transport.http.HttpSender", cb,50),
            BalancingGroupConfiguration("KafkaSender group", false, "com.transport.kafka.KafkaSender", cb, 40),
            BalancingGroupConfiguration("KafkaBuffer group", false, "com.transport.buffer.kafka.KafkaBuffer", cb, -50)
        )

        try {
            checker.checkBalancingGroupConfigs(configs)
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    @Test
    fun `check different boolean values`() {
        val testEnabled1 = TestSimpleData(null)
        val testEnabled2 = TestSimpleData(false)
        val testEnabled3 = TestSimpleData(true)

        assertTrue(testEnabled1.enabled != false)
        assertFalse(testEnabled2.enabled != false)
        assertTrue(testEnabled3.enabled != false)

    }

    data class TestSimpleData(val enabled: Boolean?)
    data class TestData(val enabled: Boolean, val client: AuditClientTestProperties)

    data class AuditClientTestProperties(
        val mode: AuditClientModeEnum,
        val nodeId: String,
        val project: String,
        val mainBalancingGroup: BalancingGroup = BalancingGroup.NONE
    )

    enum class AuditClientModeEnum { SYNC, ASYNC }
}