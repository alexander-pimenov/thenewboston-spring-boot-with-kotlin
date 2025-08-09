package tv.codealong.tutorials.various.audit

import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("audit.enabled", havingValue = "true", matchIfMissing = true)
class CommonConfigurations(
    private val auditProperties: AuditProperties
) {
    @Bean
    fun auditHandlerMap(handlers: List<AuditEventHandler<*>>): Map<HandlerName, AuditEventHandler<IProcessedInvocation>> =
        (handlers as List<AuditEventHandler<IProcessedInvocation>>).associateBy {
            HandlerName(
                it::class.qualifiedName ?: error("Отсутствует QualifiedName у класса ${it::class}")
            )
        }

    @ConditionalOnMissingBean
    @Bean
    fun auditAnnotationValidator(handlers: Map<HandlerName, AuditEventHandler<IProcessedInvocation>>): AuditAnnotationValidator =
        AuditAnnotationValidator(handlers)

    @ConditionalOnMissingBean
    @Bean
    fun auditAnnotationBeanPostProcessor(auditAnnotationValidator: AuditAnnotationValidator) =
        AuditAnnotationBeanPostProcessor(auditAnnotationValidator)

    @Bean
    fun auditingAdvisor(
        handlers: Map<HandlerName, AuditEventHandler<IProcessedInvocation>>,
        validators: Collection<AuditEventMessageValidator>,
        matchers: Collection<AuditEventMetamodelMatcher>,
        auditPointcut: AuditPointcut,
        auditSaver: AuditSaver<IProcessedInvocation>
    ): DefaultBeanFactoryPointcutAdvisor {
        val advisor = DefaultBeanFactoryPointcutAdvisor()
        advisor.setPointcut(auditPointcut)
        advisor.advice = AuditInterceptor(auditSaver, handlers, validators, matchers)
        advisor.order = Ordered.HIGHEST_PRECEDENCE + 10
        return advisor
    }

    @ConditionalOnMissingBean
    @Bean
    fun auditPointcut(): AuditPointcut {
        return BasicAuditPointcut(AnnotationMatchingPointcut(null, Auditable::class.java, true))
    }

    @Bean
    fun auditingListAdvisor(
        handlers: Map<HandlerName, AuditEventHandler<IProcessedInvocation>>,
        validators: Collection<AuditEventMessageValidator>,
        matchers: Collection<AuditEventMetamodelMatcher>,
        auditListPointcut: AuditListPointcut,
        auditSaver: AuditSaver<IProcessedInvocation>
    ): DefaultBeanFactoryPointcutAdvisor {
        val advisor = DefaultBeanFactoryPointcutAdvisor()
        advisor.setPointcut(auditListPointcut)
        advisor.advice = AuditListInterceptor(auditSaver, handlers, validators, matchers)
        advisor.order = Ordered.HIGHEST_PRECEDENCE + 10
        return advisor
    }

    @ConditionalOnMissingBean
    @Bean
    fun auditListPointcut(): AuditListPointcut {
        return BasicAuditListPointcut(AnnotationMatchingPointcut(null, AuditableList::class.java, true))
    }

    @ConditionalOnMissingBean(AuditSaver::class)
    @Bean
    fun auditSaver(auditSender: AuditSender): AuditSaver<IProcessedInvocation> {
        return if (auditProperties.client.mode == AuditClientModeEnum.ASYNC) {
            AsyncAuditSaver(auditSender)
        } else {
            SyncAuditSaver(auditSender)
        }
    }


    @ConditionalOnMissingBean(AuditMetamodelSender::class)
    @Bean
    fun auditMetamodelSender(auditSender: AuditSender) = AuditMetamodelSender(auditSender)
}

data class AuditClientProperties(
    val mode: AuditClientModeEnum,
    val nodeId: String,
    val project: String,
    val mainBalancingGroup: BalancingGroup = BalancingGroup.NONE
) {
    @ConstructorBinding
    constructor(nodeId: String?, mode: AuditClientModeEnum?, project: String?) : this(
        mode ?: AuditClientModeEnum.SYNC,
        requireNotNull((nodeId)) { "audit.client.nodeId обязателен для заполнения" },
        requireNotNull((project)) { "audit.client.project обязателен для заполнения" }) {
        require(nodeId.isNotEmpty()) { "audit.client.nodeId не должен быть пустым" }
        require(project.isNotEmpty()) { "audit.client.project не должен быть пустым" }
    }
}

//@ConfigurationProperties(prefix = "audit")
//data class AuditProperties(val enabled: Boolean, val client: AuditClientProperties, val duplicate: Boolean) {
//    @ConstructorBinding
//    constructor(client: AuditClientProperties?, enabled: Boolean?, duplicate: Boolean?) : this(
//        enabled = enabled != false, if (enabled != false) {
//            requireNotNull(client) { "блок audit.client обязателен для заполнения. (nodeId, mode, project)" }
//        } else AuditClientProperties(AuditClientModeEnum.ASYNC, "disabled", "disabled"),
//        duplicate = duplicate ?: false)
//}
//data class AuditClientProperties(
//    val mode: AuditClientModeEnum,
//    val nodeId: String,
//    val project: String,
//    val mainBalancingGroup: BalancingGroup = BalancingGroup.NONE
//)




@ConfigurationProperties(prefix = "audit.client.rest")
data class RestPropertiesBuilder(
    override val enabled: Boolean?,
    val url: String?,
    val main: Boolean = true,
    val circuitBreaker: CircuitBreakerConfiguration?
) : EnablerProperties {
    fun build(): RestProperties? {
        requireNotNull(url) { "audit.client.rest.url обязателен для заполнения" }
        require(url.isNotEmpty()) { "audit.client.rest.url обязателен для заполнения" }
        requireNotNull(circuitBreaker) { "audit.client.rest.circuitBreaker обязателен для заполнения" }
        return if (enabled == null || enabled) RestProperties(
            url,
            main,
            circuitBreaker
        ) else null
    }
}

data class RestProperties(val url: String, val main: Boolean, val circuitBreaker: CircuitBreakerConfiguration)

@ConfigurationProperties(prefix = "audit.client.kafka")
data class KafkaPropertiesBuilder(
    override val enabled: Boolean?,
    val servers: String?,
    val topics: TopicsPairBuilder?,
    val main: Boolean = false,
    val circuitBreaker: CircuitBreakerConfiguration?
) : EnablerProperties {
    fun build(): KafkaProperties? {
        requireNotNull(servers) { "сервер kafka audit.client.kafka.servers должен быть обязательно указан" }
        requireNotNull(topics) { "топики kafka audit.client.kafka.topics должны быть обязательно указаны" }
        requireNotNull(circuitBreaker) { "audit.client.kafka.circuitBreaker обязателен для заполнения" }
        return if (enabled == null || enabled) KafkaProperties(
            servers,
            topics.build(),
            main,
            circuitBreaker
        ) else null
    }
}

data class KafkaProperties(
    val servers: String,
    val topicsPair: TopicsPair,
    val main: Boolean,
    var circuitBreaker: CircuitBreakerConfiguration
)

@ConfigurationProperties(prefix = "audit.fallback")
data class FallbackPropertiesBuilder(
    override val enabled: Boolean?,
    val servers: String?,
    val topic: String?,
    val initialDelay: Long = 100,
    val delayPeriod: Long = 150,
    val groupId: String?,
    val clientId: String?,
    val circuitBreaker: CircuitBreakerConfiguration?
) : EnablerProperties {
    fun build(): FallbackProperties? {
        requireNotNull(servers) { "audit.fallback.servers обязателен для заполнения" }
        requireNotNull(topic) { "audit.fallback.topic обязателен для заполнения" }
        requireNotNull(groupId) { "audit.fallback.groupId обязателен для заполнения" }
        requireNotNull(clientId) { "audit.fallback.clientId обязателен для заполнения" }
        requireNotNull(circuitBreaker) { "audit.fallback.circuitBreaker обязателен для заполнения" }
        return if (enabled == null || enabled) FallbackProperties(
            servers,
            topic,
            initialDelay,
            delayPeriod,
            groupId,
            clientId,
            circuitBreaker
        ) else null
    }
}

data class FallbackProperties(
    val servers: String,
    val topic: String,
    val initialDelay: Long,
    val delayPeriod: Long,
    val groupId: String,
    val clientId: String,
    val circuitBreaker: CircuitBreakerConfiguration
)

data class TopicsPair(val event: String, val metamodel: String)

data class TopicsPairBuilder(val event: String?, val metamodel: String?) {
    fun build(): TopicsPair {
        requireNotNull(event) { "топик событий kafka audit.client.kafka.topics.event должен быть обязательно указан" }
        requireNotNull(metamodel) { "топик метамодели kafka audit.client.kafka.topics.metamodel должен быть обязательно указан" }
        return TopicsPair(
            event,
            metamodel
        )
    }
}
