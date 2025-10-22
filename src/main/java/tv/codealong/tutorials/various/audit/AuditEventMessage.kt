package tv.codealong.tutorials.various.audit

class AuditEventMessage(
    val id: String? = null,
    val event: RiskAuditEvent,
    val sendingRequired: Boolean = true,
)