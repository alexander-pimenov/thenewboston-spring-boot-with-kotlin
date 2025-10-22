package tv.codealong.tutorials.various.audit

import tv.codealong.tutorials.various.countDownLatch.AuditIntegration

/**
 * функция создания события аудита
 */
fun createEvent(
    integrationName: AuditIntegration,
    lambda: () -> List<RiskAuditEvent.RiskAuditEventParams>
) = AuditEventMessage()