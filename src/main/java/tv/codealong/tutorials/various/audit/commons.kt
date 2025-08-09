package tv.codealong.tutorials.various.audit

import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext

const val DEFAULT_PRIORITY = 10
const val DISABLED_VALUE = "disabled"

enum class AuditClientModeEnum { SYNC, ASYNC }
enum class BalancingGroup { NONE, REST, KAFKA, DUPLICATE; }

interface EnablerProperties {
    val enabled: Boolean?
}

abstract class CommonCondition<T : EnablerProperties> : Condition {
    fun matches(
        propertyName: String,
        context: ConditionContext,
        propertyClass: Class<T>
    ): Boolean {
        val properties = Binder
            .get(context.environment)
            .bind(propertyName, propertyClass).orElse(null)
        return properties != null && properties.enabled != false
    }
}

fun createBalancingGroupConfiguration(
    breakerConfig: CircuitBreakerConfiguration,
    groupName: String,
    providerClass: Class<*>,
    isMain: Boolean,
    priority: Int = 10
): BalancingGroupConfiguration {
    val balancingGroupConfig = BalancingGroupConfiguration()
    balancingGroupConfig.name = groupName
    balancingGroupConfig.isMain = isMain
    balancingGroupConfig.providerClass = providerClass.canonicalName
    balancingGroupConfig.breakerConfiguration = breakerConfig
    balancingGroupConfig.priority = priority
    balancingGroupConfig.providerNameToWeight[providerClass.simpleName] = 1

    return balancingGroupConfig
}

// Прочие интерфейсы, которые используются в коде
//interface KafkaPullProcessor
//interface PvmSdkEnricher
//interface PvmSdkMonitoringService
//interface PvmSignatureService
//interface HttpSender
//interface KafkaSender
//interface KafkaBuffer