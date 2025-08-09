package tv.codealong.tutorials.various.audit

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

class HttpSender(
    private val configuration: HttpSenderConfiguration,
    private val monitoringService: PvmSdkMonitoringService,
    private val restTemplate: RestTemplate = RestTemplateBuilder().build()
) : AuditSender {

    private val log = LoggerFactory.getLogger(HttpSender::class.java)
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    override fun send(event: IProcessedInvocation) {
        try {
            val route = resolveRoute(event) ?: run {
                monitoringService.trackError("No route found for event")
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

            monitoringService.trackSuccess()
        } catch (ex: Exception) {
            log.error("Failed to send audit event via HTTP", ex)
            monitoringService.trackError(ex.message ?: "Unknown error")
            throw ex
        }
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

    private fun convertHttpMethod(method: HttpMethod): org.springframework.http.HttpMethod {
        return when (method) {
            HttpMethod.GET -> org.springframework.http.HttpMethod.GET
            HttpMethod.POST -> org.springframework.http.HttpMethod.POST
            HttpMethod.PUT -> org.springframework.http.HttpMethod.PUT
            HttpMethod.DELETE -> org.springframework.http.HttpMethod.DELETE
            HttpMethod.PATCH -> org.springframework.http.HttpMethod.PATCH
            HttpMethod.HEAD -> org.springframework.http.HttpMethod.HEAD
            HttpMethod.OPTIONS -> org.springframework.http.HttpMethod.OPTIONS
        }
    }
}