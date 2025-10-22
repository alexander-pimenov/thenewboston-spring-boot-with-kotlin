package tv.codealong.tutorials.various.countDownLatch

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import tv.codealong.tutorials.various.audit.*

/**
 * Модуль формирования сообщений аудита и запуска логики отправки.
 */
class AuditProvider {

    data class EventData(
        val integration: AuditIntegration,
        val parameters: AuditParameters,
        val data: List<String>,
    ) {
        companion object {
            /**
             * метод формирования данных параметров и их меты для отправки
             */
            fun getDictionaryData(data: List<String>) = EventData(
                integration = AuditIntegration.DICTIONARY_INCOMING_EVENT,
                parameters = AuditParameters.CODE,
                data = data
            )

            fun tenantData(data: List<String>) = EventData(
                integration = AuditIntegration.TENANT_INCOMING_EVENT,
                parameters = AuditParameters.RESOURCE_NAME,
                data = data
            )
        }
    }

    companion object {
        lateinit var auditSaver: CustomSaver

        private val logger = LoggerFactory.getLogger(this::class.java)

        inline fun <T> runAuditable(
            operationName: String,
            lambda: () -> ResponseEntity<T>,
        ): ResponseEntity<T> {
            return lambda.invoke().alsoIfAuditable { result ->
                saveEvent(
                    operationName,
                    result)
            }
        }

        /**
         * обертка для аудирования вложенной логики
         */
        fun <T> runAuditable(
            operationName: String,
            version: DictionaryValueView,
            lambda: () -> T
        ): T {
            return lambda.invoke().alsoIfAuditable { result ->
                launchAsync { //эта функция приведена ниже 3)
                    val event = createEvent(AuditIntegration.VALUE_INCOMING_EVENT) {
                        getValueEventParameters(
                            code = version.code,
                            version = version.version,
                            response = if (result is ResponseEntity<*>) result else ok(result),
                            operationName = operationName
                        )
                    }
                    auditSaver.save(event)
                }
            }
        }

        fun <T> runAuditable(
            operationName: String,
            lambda: () -> T
        ): T {
            return lambda.invoke().alsoIfAuditable { result ->
                launchAsync { //эта функция приведена ниже 3)
                    val event = createEvent(AuditIntegration.VALUE_INCOMING_EVENT) {
                        getValueEventParameters(
                            code = version.code,
                            response = if (result is ResponseEntity<*>) result else ok(result),
                            operationName = operationName
                        )
                    }
                    auditSaver.save(event)
                }
            }
        }

        fun <T> T.alsoIfAuditable(block: (T) -> Unit): T {
            logger.info("Is auditable? [${::auditSaver.isInitialized}]")
            return if (::auditSaver.isInitialized) {
                this.also { block(this) }
            } else this
        }

        /**
         * метод асинхронного формирования и отправки списка сообщений аудита
         */
        fun saveEvent(defaultParameters: List<RiskAuditEvent.RiskAuditEventParams>, eventData: EventData?) {
            launchAsync {
                eventData?.data?.forEach { element ->
                    logger.info("готовимся к отправке события [$element]")
                    val event: AuditEventMessage = createEvent(eventData.integration) {

                    }
                }
            }
        }
    }
}