package tv.codealong.tutorials.various.audit

interface AuditSender {
    fun send(event: IProcessedInvocation)
}
