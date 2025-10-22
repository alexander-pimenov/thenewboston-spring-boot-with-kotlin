package tv.codealong.tutorials.various.audit

interface AuditSender {
    fun sendMeta()
    fun send(event: IProcessedInvocation)
    fun sendEvent(auditEventMessage: AuditEventMessage)

}
