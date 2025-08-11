package tv.codealong.tutorials.various.audit

import tv.codealong.tutorials.various.audit.java.BalancingGroupConfiguration

// PvmSdkConfiguration - интерфейс конфигурации SDK
interface PvmSdkConfiguration {
    val balancingGroups: List<BalancingGroupConfiguration>
    val mainBalancerMode: MainBalancerMode
    val enrichers: List<PvmSdkEnricher>
    val kafkaPullProcessors: List<KafkaPullProcessor>
    val monitoringService: PvmSdkMonitoringService
    val signatureService: PvmSignatureService
}

// PvmSdkConfigurationImpl - реализация конфигурации SDK
class PvmSdkConfigurationImpl(
    override val balancingGroups: List<BalancingGroupConfiguration>,
    override val mainBalancerMode: MainBalancerMode,
    override val enrichers: List<PvmSdkEnricher>,
    override val kafkaPullProcessors: List<KafkaPullProcessor>,
    override val monitoringService: PvmSdkMonitoringService,
    override val signatureService: PvmSignatureService
) : PvmSdkConfiguration
