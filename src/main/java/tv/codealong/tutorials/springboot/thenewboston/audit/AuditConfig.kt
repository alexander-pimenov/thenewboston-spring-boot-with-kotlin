package tv.codealong.tutorials.springboot.thenewboston.audit

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * зарегистрировать бин вручную через @EnableConfigurationProperties(RestProperties::class)
 */
@Configuration
@EnableConfigurationProperties(RestProperties::class)
class AuditConfig {
}