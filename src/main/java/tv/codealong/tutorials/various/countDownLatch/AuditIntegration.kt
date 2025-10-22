package tv.codealong.tutorials.various.countDownLatch

enum class AuditIntegration(val value: String, val description: String) {
    VALUE_INCOMING_EVENT("valueEvent", "Входящая интеграция значений"),
    DICTIONARY_INCOMING_EVENT("dictionaryEvent", "Входящая интеграция справочника"),
    TENANT_INCOMING_EVENT("tenantEvent", "Входящая интеграция тенантов")
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
