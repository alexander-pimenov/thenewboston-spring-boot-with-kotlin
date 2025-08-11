package tv.codealong.tutorials.various.audit


import io.micrometer.core.ipc.http.HttpSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tv.codealong.tutorials.various.audit.java.BalancingGroupConfiguration

@Configuration
@ConditionalOnProperty("audit.enabled", havingValue = "true", matchIfMissing = true)
class AuditSenderConfiguration(
    private val auditProperties: AuditProperties
) {
    @Bean
    fun auditSender(metamodel: AuditMetamodel, auditProxyClient: AuditProxyClient) =
        AuditSenderImpl(metamodel, auditProxyClient)
    @Bean
    fun auditProxyClient(
        restProperties: RestProperties?,
        pvmSdkConfiguration: PvmSdkConfiguration,
        httpSender: HttpSender?,
        kafkaSender: KafkaSender?,
        kafkaBuffer: KafkaBuffer?
    ) = AuditProxyClient
        .builder()
        .baseUrl(restProperties?.url ?: "undefined")
        .nodeId(auditProperties.client.nodeId)
        .setPvmSdkConfiguration(pvmSdkConfiguration, listOfNotNull(httpSender, kafkaSender, kafkaBuffer))
        .build()

    @Bean
    fun pvmSdkConfiguration(
        kafkaPullProcessor: KafkaPullProcessor?,
        balancingGroups: List<BalancingGroupConfiguration>,
        pvmSdkMonitoringService: PvmSdkMonitoringService,
        pvmSignatureService: PvmSignatureService
    ): PvmSdkConfiguration =
        PvmSdkConfigurationImpl(
            balancingGroups,
            if (auditProperties.client.mainBalancingGroup == BalancingGroup.DUPLICATE) MainBalancerMode.DUP else MainBalancerMode.STD,
            listOf<PvmSdkEnricher>(),
            listOfNotNull(kafkaPullProcessor),
            pvmSdkMonitoringService,
            pvmSignatureService
        )
}
