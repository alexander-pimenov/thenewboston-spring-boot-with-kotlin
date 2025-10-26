package tv.codealong.tutorials.various.countDownLatch

import org.hamcrest.Description

enum class ResponseStatus {
    SUCCESS, ERROR
}

enum class AuditParameters(
    val value: String,
    val description: String,
) {
    RESOURCE_NAME("resourceName", "Имя ресурса"),
    VERSION("version", "Версия справочника"),
    CODE("code", "Код справочника"),
    OPERATION_NAME("operationName", "Название интеграции"),
    RS_STATUS("rsStatus", "Сатус обработки входящей интеграции: SUCCESS, ERROR"),
    ERROR_CODE("errorCode", "Код ответа"),
}

enum class AuditIntegration(val value: String, val description: String) {
    VALUE_INCOMING_EVENT("valueEvent", "Входящая интеграция значений"),
    DICTIONARY_INCOMING_EVENT("dictionaryEvent", "Входящая интеграция справочника"),
    TENANT_INCOMING_EVENT("tenantEvent", "Входящая интеграция тенантов")
}

const val METAMODEL_VERSION = "1"
const val APPLICATION_NAME = "CI04569532_rekib_dms_dictionary_service"
const val USER_MODE = "NO_USERMODE"
const val USER_LOGIN ="test-proxy-user"
const val SUCCESS = true
const val POD_NAMESPACE = "POD_NAMESPACE"
const val SOURCE_SYSTEM = "DICTIONARY_MANAGER_SYSTEM"