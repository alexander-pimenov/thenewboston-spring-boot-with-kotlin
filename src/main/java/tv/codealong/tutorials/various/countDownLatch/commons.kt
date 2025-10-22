package tv.codealong.tutorials.various.countDownLatch

import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import tv.codealong.tutorials.various.audit.AuditSender
import tv.codealong.tutorials.various.audit.launchAsync
import tv.codealong.tutorials.various.countDownLatch.AuditProvider.Companion.alsoIfAuditable



