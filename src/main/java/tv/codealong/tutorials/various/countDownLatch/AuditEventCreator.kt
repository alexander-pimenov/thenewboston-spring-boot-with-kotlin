package tv.codealong.tutorials.various.countDownLatch

import org.springframework.http.ResponseEntity
import tv.codealong.tutorials.various.audit.RiskAuditEvent
import tv.codealong.tutorials.various.model.Tenant

fun String?.getOrDefault() = this ?: "Значение не передано"

fun ResponseEntity<*>.getDefaultParameters(operationName: String): List<RiskAuditEvent.RiskAuditEventParams> = listOf(
    RiskAuditEvent.RiskAuditEventParams(
        name = AuditParameters.RS_STATUS.value,
        value = if (statusCode.is2xxSuccessful) ResponseStatus.SUCCESS.name else ResponseStatus.ERROR.name
    ),
    RiskAuditEvent.RiskAuditEventParams(
        name = AuditParameters.ERROR_CODE.value,
        value = statusCode.value().toString()
    ),
    RiskAuditEvent.RiskAuditEventParams(
        name = AuditParameters.OPERATION_NAME.value,
        value = operationName
    )
)

/**
 * Метод для получения параметров события аудита контроллера значений тенантов справочника
 */
fun getTenantEventParameters(
    operationName: String,
    response: ResponseEntity<Tenant>
): List<RiskAuditEvent.RiskAuditEventParams> {
    return response.getDefaultParameters(operationName) +
            RiskAuditEvent.RiskAuditEventParams(
                name = AuditParameters.RESOURCE_NAME.value,
                value = response.body?.resourceName.getOrDefault()
            )
}

fun getValueEventParameters(
    code: String,
    version: String,
    response: ResponseEntity<*>,
    operationName: String
): List<RiskAuditEvent.RiskAuditEventParams> {
    return response.getDefaultParameters(operationName) + listOf(
        RiskAuditEvent.RiskAuditEventParams(AuditParameters.CODE.value, code),
        RiskAuditEvent.RiskAuditEventParams(AuditParameters.VERSION.value, version)
    )
}