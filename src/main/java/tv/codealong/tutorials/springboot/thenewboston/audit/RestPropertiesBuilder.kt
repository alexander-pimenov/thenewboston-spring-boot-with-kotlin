package tv.codealong.tutorials.springboot.thenewboston.audit

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * В Kotlin обычно валидацию делают именно в init, потому что это основной механизм для выполнения
 * логики сразу после инициализации всех свойств первичного конструктора.
 * Вторичные конструкторы нужны для создания дополнительных способов инициализации, а не для валидации.
 *
 * <p>
 * простое наличие @ConfigurationProperties не создаёт бин само по себе.
 *
 * Spring Boot с версии 2.2 и выше требует, чтобы класс с @ConfigurationProperties был либо:
 *
 * Помечен как компонент (@Component),
 *
 * Или зарегистрирован через @EnableConfigurationProperties(RestProperties::class),
 *
 * Или был возвращён как бин из конфигурации (@Bean).
 *
 * Иначе Spring просто не будет знать, что нужно создать бин для твоего класса.
 */
@ConfigurationProperties(prefix = "audit.client.rest")
data class RestPropertiesBuilder(
    override val enabled: Boolean?,
    val url: String?,
    val circuitBreaker: CircuitBreakerConfiguration?,
    val priority: Int = 10
) : EnablerProperties {

    fun build(): RestProperties {
        requireNotNull(url) { "audit.client.rest.url обязателен для заполнения" }
        require(url.isNotEmpty()) { "audit.client.rest.url обязателен для заполнения" }
        require(url.startsWith("http://") || url.startsWith("https://")) {
            "audit.client.rest.url не начинается с http или https"
        }
        requireNotNull(circuitBreaker) { "audit.client.rest.circuitBreaker обязателен для заполнения" }
        return RestProperties(url, circuitBreaker, priority)
    }
}


data class RestProperties(
    val url: String,
    val circuitBreaker: CircuitBreakerConfiguration,
    val priority: Int
)