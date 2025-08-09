package tv.codealong.tutorials.various.audit

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("audit.enabled", havingValue = "true", matchIfMissing = true)
class AuditConfigValidator(
    private val environment: Environment,
    private val auditProperties: AuditProperties,
    private val restPropertiesBuilder: RestPropertiesBuilder?,
    private val kafkaPropertiesBuilder: KafkaPropertiesBuilder?
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        validateConfiguration()
    }

    private fun validateConfiguration() {
        when (auditProperties.client.mainBalancingGroup) {
            BalancingGroup.REST -> validateRestConfig()
            BalancingGroup.KAFKA -> validateKafkaConfig()
            BalancingGroup.DUPLICATE -> validateDuplicateConfig()
            BalancingGroup.NONE -> {} // Ничего не проверяем
        }
    }

    private fun validateRestConfig() {
        val value = restPropertiesBuilder?.enabled ?: false
        require(value) {
            "Для mainBalancingGroup=REST необходимо включить REST конфигурацию (audit.client.rest.enabled=true)"
        }
        validateRestProperties()
    }

    private fun validateKafkaConfig() {
        val value = kafkaPropertiesBuilder?.enabled ?: false
        require(value) {
            "Для mainBalancingGroup=KAFKA необходимо включить Kafka конфигурацию (audit.client.kafka.enabled=true)"
        }
        validateKafkaProperties()
    }

    private fun validateDuplicateConfig() {
        val value = restPropertiesBuilder?.enabled ?: false
        val value2 = kafkaPropertiesBuilder?.enabled ?: false
        require(value && value2) {
            "Для mainBalancingGroup=DUPLICATE необходимо включить и REST и Kafka конфигурации " +
                    "(audit.client.rest.enabled=true и audit.client.kafka.enabled=true)"
        }
        validateRestProperties()
        validateKafkaProperties()
    }

    private fun validateRestProperties() {
        val restUrl = restPropertiesBuilder?.url ?: ""
        require(restUrl.isNotBlank()) {
            "Не указан URL для REST (audit.client.rest.url)"
        }
        // Дополнительные проверки для REST
    }

    private fun validateKafkaProperties() {
        val kafkaServers = kafkaPropertiesBuilder?.servers ?: ""
        require(kafkaServers.isNotBlank()) {
            "Не указаны серверы Kafka (audit.client.kafka.servers)"
        }
        val topicsPairBuilder = kafkaPropertiesBuilder?.topics
        if (topicsPairBuilder != null) {
            topicsPairBuilder.event?.isNotBlank()?.let {
                require(it) {
                    "Не указана тема для событий (audit.client.kafka.topics.event)"
                }
            }
            topicsPairBuilder.metamodel?.isNotBlank()?.let {
                require(it) {
                    "Не указана тема для метамодели (audit.client.kafka.topics.metamodel)"
                }
            }
        }

        // Дополнительные проверки для Kafka
    }
}