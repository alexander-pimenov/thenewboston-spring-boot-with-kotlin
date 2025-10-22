package tv.codealong.tutorials.various.countDownLatch

import tv.codealong.tutorials.various.audit.AuditEventMessage

interface CustomSaver {
    fun save(eventMessage: AuditEventMessage): Any
}
