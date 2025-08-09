package tv.codealong.tutorials.various.audit

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.AnnotatedTypeMetadata
import java.util.regex.Pattern

@Configuration
@Conditional(RestBalancingGroupConfiguration.RestCondition::class)
@EnableConfigurationProperties(RestPropertiesBuilder::class)
@ConditionalOnProperty("audit.enabled", havingValue = "true", matchIfMissing = true)
class RestBalancingGroupConfiguration(
    private val auditProperties: AuditProperties,
    private val restPropertiesBuilder: RestPropertiesBuilder
) {

    class RestCondition : CommonCondition<RestPropertiesBuilder>() {
        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata) =
            matches("audit.client.rest", context, RestPropertiesBuilder::class.java)
    }

    @Bean
    fun restProperties() = restPropertiesBuilder.build()

    @Bean
    fun httpSenderBalancingGroupConfiguration(
        restProperties: RestProperties
    ) =
        createBalancingGroupConfiguration(
            restProperties.circuitBreaker,
            "HttpSender group",
            HttpSender::class.java,
            restProperties.main
        )

    @Bean
    fun httpSender(
        httpSenderConfiguration: HttpSenderConfiguration,
        pvmSdkMonitoringService: PvmSdkMonitoringService
    ) =
        HttpSender(
            httpSenderConfiguration,
            pvmSdkMonitoringService,
            RestTemplateBuilder().build()
//            null
        )

    @Bean
    fun httpSenderConfiguration(
        restProperties: RestProperties
    ): HttpSenderConfiguration {
        val auditUrl = restProperties.url
        val project = auditProperties.client.project
        val httpSenderConfig = HttpSenderConfiguration()
        httpSenderConfig.name = HttpSender::class.simpleName
        httpSenderConfig.routeResolvers = listOf(
            TransportRouteConfig(
                "payload_type", Pattern.compile("metamodel"), "metamodel_pipeline",
                false
            ),
            TransportRouteConfig(
                "payload_type", Pattern.compile("event"), "event_pipeline",
                false
            )
        )
        httpSenderConfig.baseHeaders = mapOf(
            "X-Node-ID" to auditProperties.client.nodeId,
            "Content-Type" to "application/json"
        )
        httpSenderConfig.properties = mapOf(HttpSenderConfiguration.BASE_URL_PROPERTY to auditUrl)
        httpSenderConfig.setRoutes(
            listOf(
                HttpRoute(
                    "metamodel_pipeline",
                    String.format("push/project/%s/split-by-ott/v2/metamodel", project),
                    HttpMethod.POST
                ),
                HttpRoute(
                    "event_pipeline",
                    String.format("push/project/%s/split-by-ott/v2/event", project),
                    HttpMethod.POST
                )
            )
        )
        return httpSenderConfig
    }
}