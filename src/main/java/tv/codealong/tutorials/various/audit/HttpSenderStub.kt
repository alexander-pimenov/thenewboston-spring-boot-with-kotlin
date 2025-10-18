package tv.codealong.tutorials.various.audit

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import feign.Request
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import tv.codealong.tutorials.various.audit.java.HttpRoute
import tv.codealong.tutorials.various.audit.java.PvmSdkMonitoringService

/**
 * Реализует AuditSender для отправки через HTTP
 *
 * Использует RestTemplate для выполнения запросов
 *
 * Поддерживает маршрутизацию на разные endpoints
 *
 * Интегрирован с мониторингом
 */
class HttpSenderStub(
    private val configuration: HttpSenderConfiguration,
    private val monitoringService: PvmSdkMonitoringService,
    private val restTemplate: RestTemplate = RestTemplateBuilder().build()
) : AuditSender {

    private val log = LoggerFactory.getLogger(HttpSenderStub::class.java)
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    override fun sendMeta() {
        TODO("Not yet implemented")
    }

    override fun send(event: IProcessedInvocation) {
        try {
            val route = resolveRoute(event) ?: run {
                monitoringService.messageLost()
                throw IllegalStateException("No route found for event")
            }

            val url = buildUrl(route)
            val headers = buildHeaders(event)
            val body = serializeEvent(event)

            val response = restTemplate.exchange(
                url,
                convertHttpMethod(route.method),
                HttpEntity(body, headers),
                String::class.java
            )

            if (!response.statusCode.is2xxSuccessful) {
                throw RuntimeException("HTTP request failed with status: ${response.statusCode}")
            }

            monitoringService.messageLost()
        } catch (ex: Exception) {
            log.error("Failed to send audit event via HTTP", ex)
            monitoringService.messageLost()
            throw ex
        }
    }

    override fun sendEvent(auditEventMessage: AuditEventMessage) {
        TODO("Not yet implemented")
    }

    private fun resolveRoute(event: IProcessedInvocation): HttpRoute? {
        val routeName = configuration.routeResolvers.firstOrNull { route ->
            val value = event.getHeader(route.headerName)?.toString() ?: return@firstOrNull false
            route.pattern.matcher(value).matches()
        }?.topic ?: return null

        return configuration.getRoute(routeName)
    }

    private fun buildUrl(route: HttpRoute): String {
        val baseUrl = configuration.properties[HttpSenderConfiguration.BASE_URL_PROPERTY]!!
        return "$baseUrl/${route.path}".replace("//", "/")
    }

    private fun buildHeaders(event: IProcessedInvocation): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        configuration.baseHeaders.forEach { (key, value) -> headers.add(key, value) }
        return headers
    }

    private fun serializeEvent(event: IProcessedInvocation): String {
        return objectMapper.writeValueAsString(event)
    }

    private fun convertHttpMethod(method: Request.HttpMethod): org.springframework.http.HttpMethod {
        return when (method) {
            Request.HttpMethod.GET -> org.springframework.http.HttpMethod.GET
            Request.HttpMethod.POST -> org.springframework.http.HttpMethod.POST
            Request.HttpMethod.PUT -> org.springframework.http.HttpMethod.PUT
            Request.HttpMethod.DELETE -> org.springframework.http.HttpMethod.DELETE
            Request.HttpMethod.PATCH -> org.springframework.http.HttpMethod.PATCH
            Request.HttpMethod.HEAD -> org.springframework.http.HttpMethod.HEAD
            Request.HttpMethod.OPTIONS -> org.springframework.http.HttpMethod.OPTIONS
            else -> {
                error("Unsupported HTTP method: $method")
            }
        }
    }
}