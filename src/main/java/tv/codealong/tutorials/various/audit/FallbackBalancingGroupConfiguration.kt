package tv.codealong.tutorials.various.audit

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.AnnotatedTypeMetadata

@Configuration
@EnableConfigurationProperties(FallbackPropertiesBuilder::class)
@Conditional(FallbackBalancingGroupConfiguration.FallbackCondition::class)
@ConditionalOnProperty("audit.enabled", havingValue = "true", matchIfMissing = true)
class FallbackBalancingGroupConfiguration(
    private val fallbackPropertiesBuilder: FallbackPropertiesBuilder
) {
    class FallbackCondition : CommonCondition<FallbackPropertiesBuilder>() {
        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata) =
            matches("audit.fallback", context, FallbackPropertiesBuilder::class.java)
    }

    @Bean
    fun fallbackProperties() = fallbackPropertiesBuilder.build()

    @Bean
    fun kafkaBufferBalancingGroupConfiguration(
        fallbackProperties: FallbackProperties
    ) =
        createBalancingGroupConfiguration(
            fallbackProperties.circuitBreaker,
            "KafkaBuffer group",
            KafkaBuffer::class.java,
            false,
            priority = 0
        )

    @Bean
    fun kafkaPullProcessorConfiguration(
        fallbackProperties: FallbackProperties
    ): KafkaPullProcessorConfiguration {
        val kafkaPullProcessorConfiguration = KafkaPullProcessorConfiguration()
        kafkaPullProcessorConfiguration.topic = fallbackProperties.topic
        kafkaPullProcessorConfiguration.initialDelayPeriod = fallbackProperties.initialDelay
        kafkaPullProcessorConfiguration.delayPeriod = fallbackProperties.delayPeriod
        kafkaPullProcessorConfiguration.properties = mapOf(
            "bootstrap.servers" to fallbackProperties.servers,
            "client.id" to fallbackProperties.clientId,
            "group.id" to fallbackProperties.groupId
        )
        return kafkaPullProcessorConfiguration
    }

    @Bean
    fun kafkaPullProcessor(
        kafkaPullProcessorConfiguration: KafkaPullProcessorConfiguration,
        pvmSdkMonitoringService: PvmSdkMonitoringService,
        pvmSignatureService: PvmSignatureService
    ): KafkaPullProcessor {
        return KafkaPullProcessor(
            kafkaPullProcessorConfiguration,
            pvmSdkMonitoringService,
            pvmSignatureService
        )
    }

    @Bean
    fun kafkaBuffer(
        kafkaBufferConfiguration: KafkaBufferConfiguration,
        pvmSdkMonitoringService: PvmSdkMonitoringService,
        pvmSignatureService: PvmSignatureService,
        kafkaPullProcessor: KafkaPullProcessor
    ) =
        KafkaBuffer(
            kafkaBufferConfiguration,
            pvmSdkMonitoringService,
            pvmSignatureService,
            kafkaPullProcessor.consumer
        )

    @Bean
    fun kafkaBufferConfiguration(
        kafkaBufferSenderConfiguration: KafkaBufferSenderConfiguration,
        fallbackProperties: FallbackProperties
    ): KafkaBufferConfiguration {
        val kafkaBufferConfig = KafkaBufferConfiguration()
        kafkaBufferConfig.sender = kafkaBufferSenderConfiguration
        kafkaBufferConfig.maxConsumerLagLimit = 1000L
        kafkaBufferConfig.topic = fallbackProperties.topic
        kafkaBufferConfig.consumerLagInitialDelayPeriodMs = 60000L
        kafkaBufferConfig.consumerLagDelayMs = 60000L

        return kafkaBufferConfig
    }

    @Bean
    fun kafkaBufferSenderConfiguration(
        fallbackProperties: FallbackProperties
    ): KafkaBufferSenderConfiguration {
        val kafkaSenderConfig = KafkaBufferSenderConfiguration()
        kafkaSenderConfig.name = KafkaBuffer::class.simpleName
        kafkaSenderConfig.baseHeaders = mapOf("header_name" to "header_value")
        kafkaSenderConfig.properties = mapOf(
            "bootstrap.servers" to fallbackProperties.servers,
            "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
            "value.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer"
        )

        return kafkaSenderConfig
    }

    @Bean(destroyMethod = "close")
    fun kafkaPullProcessingScheduler(
        kafkaPullProcessor: KafkaPullProcessor,
        kafkaPullProcessorConfiguration: KafkaPullProcessorConfiguration,
        pvmSdkMonitoringService: PvmSdkMonitoringService,
        auditProxyClient: AuditProxyClient
    ) =
        KafkaPullProcessingScheduler(
            kafkaPullProcessor,
            kafkaPullProcessorConfiguration,
            pvmSdkMonitoringService,
            auditProxyClient.balancingSender
        )
}