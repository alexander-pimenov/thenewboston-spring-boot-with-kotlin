package tv.codealong.tutorials.various.countDownLatch

import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import tv.codealong.tutorials.various.audit.AuditSender
import tv.codealong.tutorials.various.audit.launchAsync

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

