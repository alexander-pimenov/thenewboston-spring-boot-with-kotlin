package tv.codealong.tutorials.various.audit

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.AnnotatedTypeMetadata
import java.util.regex.Pattern
import tv.codealong.tutorials.various.audit.java.PvmSdkMonitoringService

@Configuration
@Conditional(KafkaBalancingGroupConfiguration.KafkaCondition::class)
@EnableConfigurationProperties(KafkaPropertiesBuilder::class)
@ConditionalOnProperty("audit.enabled", havingValue = "true", matchIfMissing = true)
class KafkaBalancingGroupConfiguration(
    private val kafkaPropertiesBuilder: KafkaPropertiesBuilder
) {

    class KafkaCondition : CommonCondition<KafkaPropertiesBuilder>() {
        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata) =
            matches("audit.client.kafka", context, KafkaPropertiesBuilder::class.java)
    }

    @Bean
    fun kafkaProperties() = kafkaPropertiesBuilder.build()

    @Bean
    fun kafkaSenderBalancingGroupConfiguration(
        kafkaProperties: KafkaProperties
    ) =
        createBalancingGroupConfiguration(
            kafkaProperties.circuitBreaker,
            "KafkaSender group",
            KafkaSender::class.java,
            kafkaProperties.main
        )

    @Bean
    fun kafkaSender(
        kafkaSenderConfiguration: KafkaSenderConfiguration,
        pvmSdkMonitoringService: PvmSdkMonitoringService
    ) =
        KafkaSender(kafkaSenderConfiguration, pvmSdkMonitoringService)

    //Это для примера, если нужно передавать бин с указанием Qualifier
//    @Bean
//    fun kafkaSender(
//        kafkaSenderConfiguration: KafkaSenderConfiguration,
//        pvmSdkMonitoringService: PvmSdkMonitoringService,
//        @Qualifier("kafkaObjectMapper") objectMapper: ObjectMapper
//    ) = KafkaSender(kafkaSenderConfiguration, pvmSdkMonitoringService, objectMapper)

    @Bean
    fun kafkaSenderConfiguration(
        kafkaProperties: KafkaProperties
    ): KafkaSenderConfiguration {
        val kafkaSenderConfig = KafkaSenderConfiguration()
        kafkaSenderConfig.name = KafkaSender::class.simpleName
        kafkaSenderConfig.baseHeaders = mapOf()
        kafkaSenderConfig.routeResolvers = listOf(
            TransportRouteConfig(
                "payload_type",
                Pattern.compile("metamodel"),
                kafkaProperties.topicsPair.metamodel,
                false
            ),
            TransportRouteConfig(
                "payload_type",
                Pattern.compile("event"),
                kafkaProperties.topicsPair.event,
                false
            )
        )
        kafkaSenderConfig.properties = mapOf<String, String>(
            "bootstrap.servers" to kafkaProperties.servers,
            "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
            "value.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer",
            "ssl.engine.factory.class" to "com.sber.tech.pvm.transport.ssl.kafka.PvmSdkKafkaSslEngineFactory"
        )
        return kafkaSenderConfig
    }
}