package tv.codealong.tutorials.various.audit

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Использует KafkaProducer для отправки сообщений
 *
 * Поддерживает маршрутизацию сообщений на разные топики
 *
 * Интегрирован с мониторингом через PvmSdkMonitoringService
 *
 * Реализует интерфейс AuditSender из предыдущего примера
 *
 * Использовали предварительно настроенный ObjectMapper с поддержкой:
 *
 * Kotlin классов (через jacksonObjectMapper())
 *
 * Java Time API (даты/время)
 *
 * Отключили запись дат как timestamp
 *
 * Игнорирования null полей
 */
class KafkaSender(
    private val configuration: KafkaSenderConfiguration,
    private val monitoringService: PvmSdkMonitoringService,
    //private val objectMapper: ObjectMapper
    //так можно  добавить сразу и настроить ObjectMapper , но лучше через создание бина в конфигклассе
//    private val objectMapper: ObjectMapper = jacksonObjectMapper()
//        .registerModule(JavaTimeModule())
//        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
//        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
) : AuditSender {

    private val producer: KafkaProducer<String, ByteArray> by lazy {
        val props = Properties()
        props.putAll(configuration.properties)
        KafkaProducer<String, ByteArray>(props)
    }

    private val log = LoggerFactory.getLogger(KafkaSender::class.java)

    override fun send(event: IProcessedInvocation) {
        try {
            val route = resolveRoute(event) ?: run {
                monitoringService.trackError("No route found for event")
                throw IllegalStateException("No route found for event")
            }

            val record = ProducerRecord(
                route.topic,
                null,
                System.currentTimeMillis(),
                event.getKey(),
                serializeEvent(event),
                buildHeaders(event)
            )

            val future = producer.send(record)
            future.get() // Для синхронной отправки

            monitoringService.trackSuccess()
        } catch (ex: Exception) {
            log.error("Failed to send audit event to Kafka", ex)
            monitoringService.trackError(ex.message ?: "Unknown error")
            throw ex
        }
    }

    private fun resolveRoute(event: IProcessedInvocation): TransportRouteConfig? {
        return configuration.routeResolvers.firstOrNull { route ->
            val value = event.getHeader(route.headerName)?.toString() ?: return@firstOrNull false
            route.pattern.matcher(value).matches()
        }
    }

//    private fun serializeEvent(event: IProcessedInvocation): ByteArray {
//        // Реализация сериализации события в byte[]
//        return Json.encodeToString(event).toByteArray()
//    }

    private fun serializeEvent(event: IProcessedInvocation): ByteArray {
        return try {
            configureObjectMapper().writeValueAsBytes(event)
        } catch (ex: Exception) {
            throw SerializationException("Failed to serialize audit event", ex)
        }
    }

    private fun buildHeaders(event: IProcessedInvocation): List<Header> {
        return configuration.baseHeaders.map { (key, value) ->
            RecordHeader(key, value.toByteArray())
        }
    }

    fun close() {
        producer.close()
    }

    companion object {
        //настройка для ObjectMapper
        fun configureObjectMapper(): ObjectMapper {
            return jacksonObjectMapper()
                .registerModule(JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}
