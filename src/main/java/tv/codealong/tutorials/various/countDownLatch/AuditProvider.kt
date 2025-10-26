package tv.codealong.tutorials.various.countDownLatch

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import tv.codealong.tutorials.various.audit.*
import tv.codealong.tutorials.various.countDownLatch.AuditProvider.EventData.Companion.getDictionaryData
import tv.codealong.tutorials.various.dao.version.DictionaryHistory
import tv.codealong.tutorials.various.dao.version.DictionaryVersion
import tv.codealong.tutorials.various.model.Dictionary
import tv.codealong.tutorials.various.model.DictionaryHistoryList
import tv.codealong.tutorials.various.model.DictionaryHistoryPage
import tv.codealong.tutorials.various.model.DictionaryList
import tv.codealong.tutorials.various.model.DictionaryPage
import tv.codealong.tutorials.various.model.DictionaryValue
import tv.codealong.tutorials.various.model.DictionaryValueView
import tv.codealong.tutorials.various.model.DictionaryValueViewList
import tv.codealong.tutorials.various.model.Tenant
import tv.codealong.tutorials.various.model.TenantList

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

        val logger = LoggerFactory.getLogger(this::class.java)

        /**
         * обертка для аудирования вложенной логики (1)
         */
        inline fun <T> runAuditable(
            operationName: String,
            lambda: () -> ResponseEntity<T>,
        ): ResponseEntity<T> {
            return lambda.invoke().alsoIfAuditable { result ->
                saveEvent(
                    defaultParameters = result.getDefaultParameters(operationName),
                    eventData = provideParametersData(result)
                ).also {
                    logger.info("Start audit send...")
                }
            }
        }

        /**
         * обертка для аудирования вложенной логики (2)
         */
        inline fun <T> runAuditable(
            operationName: String,
            auditIntegration: AuditIntegration,
            lambda: () -> ResponseEntity<T>
        ): ResponseEntity<T> {
            return lambda.invoke().alsoIfAuditable { result ->
                launchAsync { //эта функция приведена ниже 3)
                    saveEvent(
                        defaultParameters = result.getDefaultParameters(operationName),
                        data = provideParameters(response = result),
                        auditIntegration = auditIntegration
                    )
                }
            }
        }

        /**
         * обертка для аудирования вложенной логики (3)
         */
        fun runAuditable(
            operationName: String,
            version: DictionaryVersion,
            dictionary: DictionaryHistory,
            lambda: () -> ResponseEntity<DictionaryValue>
        ): ResponseEntity<DictionaryValue> {
            return lambda.invoke().alsoIfAuditable { result ->
                launchAsync { //эта функция приведена ниже 3)
                    val event = createEvent(AuditIntegration.VALUE_INCOMING_EVENT) {
                        getValueEventParameters(
                            code = dictionary.code,
                            version = version.version,
                            response = result,
                            operationName = operationName
                        )
                    }
                    auditSaver.save(event)
                }
            }
        }

        /**
         * обертка для аудирования вложенной логики (4)
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

        fun <T> T.alsoIfAuditable(block: (T) -> Unit): T {
            logger.info("Is auditable? [${::auditSaver.isInitialized}]")
            return if (::auditSaver.isInitialized) {
                this.also { block(this) }
            } else this
        }

        private fun getViewData(response: DictionaryValueViewList): List<Map<AuditParameters, String>> =
            response.list.map {
                mapOf(
                    AuditParameters.CODE to it.code,
                    AuditParameters.VERSION to it.version
                )
            }

        /**
         * метод асинхронного формирования и отправки списка сообщений аудита
         */
        fun saveEvent(
            defaultParameters: List<RiskAuditEvent.RiskAuditEventParams>,
            eventData: EventData?
        ) {
            launchAsync {
                eventData?.data?.forEach { element ->
                    logger.info("готовимся к отправке события [$element]")
                    val event: AuditEventMessage = createEvent(integrationName = eventData.integration) {
                        defaultParameters + RiskAuditEvent.RiskAuditEventParams(
                            name = eventData.parameters.value,
                            value = element.getOrDefault()
                        )
                    }
                    logger.info("готовимся к отправке ивента [$event]")
                    auditSaver.save(event).also {
                        logger.info("Save audit event [$event]")
                    }
                }
            }
        }

        /**
         * метод асинхронного формирования и отправки списка сообщений аудита с множественными параметрами
         */
        fun saveEvent(
            defaultParameters: List<RiskAuditEvent.RiskAuditEventParams>,
            data: List<Map<AuditParameters, String>>?,
            auditIntegration: AuditIntegration
        ) {
            launchAsync {
                data?.forEach { element ->
                    logger.info("готовимся к отправке события [$element]")
                    val event: AuditEventMessage = createEvent(integrationName = auditIntegration) {
                        defaultParameters + element
                            .map { RiskAuditEvent.RiskAuditEventParams(it.key.value, it.value.getOrDefault()) }
                            .toList()
                    }
                    logger.info("готовимся к отправке ивента [$event]")
                    auditSaver.save(event).also {
                        logger.info("Save audit event [$event]")
                    }
                }
            }
        }

        /**
         * метод для получения параметров события аудита контроллера значений справочника и тенантов
         */
        fun <T> provideParametersData(response: ResponseEntity<T>): EventData? {
            return when (val result = response.body) {
                is DictionaryHistoryList -> getDictionaryData(data = result.list.map { it.code })
                is DictionaryList -> getDictionaryData(data = result.list.map { it.code })
                is DictionaryHistoryPage -> getDictionaryData(data = result.list.map { it.code })
                is DictionaryPage -> getDictionaryData(data = result.list.map { it.code })
                is Dictionary -> getDictionaryData(data = listOf(result.code))
                is TenantList -> EventData.tenantData(result.list.map { it.resourceName })
                is Tenant -> EventData.tenantData(listOf(result.resourceName))
                else -> null
            }
        }

        /**
         * метод для получения параметров события аудита контроллера значений справочника
         */
        fun <T> provideParameters(response: ResponseEntity<T>): List<Map<AuditParameters, String>>? {
            return when (val result = response.body) {
                is DictionaryValueViewList -> getViewData(result)
                else -> null
            }
        }
    }
}

