package tv.codealong.tutorials.various.audit

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("audit.enabled.client.mainBalancingGroup", havingValue = "DUPLICATE", matchIfMissing = true)
class AuditConfigurationValidator(
    private val auditProperties: AuditProperties,
    private val kafkaPropertiesBuilder: KafkaPropertiesBuilder,
    private val restPropertiesBuilder: RestPropertiesBuilder
) : ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        validateAuditConfig()
    }

    private fun validateAuditConfig() {
        when (auditProperties.client.mainBalancingGroup) {
            BalancingGroup.DUPLICATE -> {
                require(
                    (restPropertiesBuilder.enabled == null || restPropertiesBuilder.enabled != false)
                            && (kafkaPropertiesBuilder.enabled == null || kafkaPropertiesBuilder.enabled != false)
                ) {
                    """
                        Для mainBalancingGroup=DUPLICATE требуется включение обеих конфигураций (audit.client.rest и audit.client.kafka).                    
                        Сейчас:                    
                        - audit.client.rest.enabled=${if (restPropertiesBuilder.enabled == null) "true (не указан)" else restPropertiesBuilder.enabled.toString()}                    
                        - audit.client.kafka.enabled=${if (kafkaPropertiesBuilder.enabled == null) "true (не указан)" else kafkaPropertiesBuilder.enabled.toString()}                    
                        Обратите внимание: если параметр 'enabled' не указан в application.yaml, он автоматически устанавливается как 'true'.                    
                    """.trimIndent()
                }
                validateRestProperties()
                validateKafkaProperties()
            }

            BalancingGroup.REST -> {}
            BalancingGroup.KAFKA -> {}
            else -> error("Для mainBalancingGroup установлено неизвестное значение=${auditProperties.client.mainBalancingGroup}")
        }
    }

    private fun validateRestProperties() {
        requireNotNull(restPropertiesBuilder.url) { "audit.client.rest.url обязателен для заполнения" }
        require(restPropertiesBuilder.url.isNotEmpty()) { "audit.client.rest.url обязателен для заполнения" }
        requireNotNull(restPropertiesBuilder.circuitBreaker) { "audit.client.rest.circuitBreaker обязателен для заполнения" }
    }

    private fun validateKafkaProperties() {
        requireNotNull(kafkaPropertiesBuilder.servers) { "сервер kafka audit.client.kafka.servers должен быть обязательно указан" }
        requireNotNull(kafkaPropertiesBuilder.topics) { "топики kafka audit.client.kafka.topics должны быть обязательно указаны" }
        requireNotNull(kafkaPropertiesBuilder.circuitBreaker) { "audit.client.kafka.circuitBreaker обязателен для заполнения" }
    }
}

@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@AutoConfiguration
@Import(
    PvmSdkServicesConfiguration::class,
    MetamodelConfiguration::class,
    EventMessageValidatorConfiguration::class,
    RestBalancingGroupConfiguration::class,
    KafkaBalancingGroupConfiguration::class,
    FallbackBalancingGroupConfiguration::class,
    AuditSenderConfiguration::class,
    CommonConfigurations::class,
    AuditConfigurationValidator::class
)
@EnableConfigurationProperties(
    value = [
        AuditProperties::class,
        RestPropertiesBuilder::class,
        KafkaPropertiesBuilder::class
    ]
)
class AuditAutoConfiguration


//Я его хочу использовать для проверки заполненности полей из application.yaml для параметра
// 'auditProperties.client.mainBalancingGroup=DUPLICATE'.Тест падает с ошибкой:Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException:
// Error creating bean with name 'ru.sberbank.riskexpertise.audit.autoconfigure.AuditConfigurationValidator': Unsatisfied dependency expressed through constructor parameter 2: No qualifying bean of type 'ru.sberbank.riskexpertise.audit.autoconfigure.RestPropertiesBuilder' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}