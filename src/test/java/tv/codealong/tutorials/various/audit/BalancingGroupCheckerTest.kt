package tv.codealong.tutorials.various.audit

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tv.codealong.tutorials.various.audit.java.BalancingGroupConfiguration

class BalancingGroupCheckerTest {

    @Test
    fun `should return true if balancing group is NONE`() {
        val checker = BalancingGroupChecker()
        val element1 = BalancingGroupConfiguration()
        element1.name = "HttpSender group"
        element1.isMain = false
        element1.providerClass = "com.transport.http.HttpSender"
        element1.priority = 20
        val element2 = BalancingGroupConfiguration()
        element2.name = "KafkaBuffer group"
        element2.isMain = false
        element2.providerClass = "com.transport.buffer.kafka.KafkaBuffer"
        element2.priority = -100
        val element3 = BalancingGroupConfiguration()
        element3.name = "KafkaSender group"
        element3.isMain = false
        element3.providerClass = "com.transport.kafka.KafkaSender"
        element3.priority = 30
        val element4 = BalancingGroupConfiguration()
        element4.name = "HttpSender group"
        element4.isMain = false
        element4.providerClass = "com.transport.http.HttpSender"
        element4.priority = 50
        val element5 = BalancingGroupConfiguration()
        element5.name = "KafkaSender group"
        element5.isMain = false
        element5.providerClass = "com.transport.kafka.KafkaSender"
        element5.priority = 40
        val element6 = BalancingGroupConfiguration()
        element6.name = "KafkaBuffer group"
        element6.isMain = false
        element6.providerClass = "com.transport.buffer.kafka.KafkaBuffer"
        element6.priority = -50
        val configs = listOf(element1, element2, element3, element4, element5, element6)

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

//    data class BalancingGroupConfiguration(
//        var name: String,
//        var isMain: Boolean,
//        var providerClass: String,
//        var breakerConfiguration: tv.codealong.tutorials.various.audit.java.CircuitBreakerConfiguration,
//        var priority: Int,
//        var providerNameToWeight: MutableMap<String, Int> = mutableMapOf()
//
//    )
}