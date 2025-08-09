package tv.codealong.tutorials.springboot.thenewboston.audit.another

import org.springframework.boot.context.properties.ConfigurationProperties
import tv.codealong.tutorials.springboot.thenewboston.audit.CircuitBreakerConfiguration
import tv.codealong.tutorials.springboot.thenewboston.audit.EnablerProperties

/**
 * requireNotNull для circuitBreaker можно опустить, если сделать его не-nullable в конструкторе.
 * Теперь при биндинге Spring Boot автоматически создаст бин RestProperties, и в случае некорректных
 * данных выбросится исключение на этапе старта.
 * <p>
 * Минусы:
 * - Валидация сработает при биндинге свойств. Если нужны более гибкие сценарии, Builder иногда удобнее.
 * <p>
 * Плюсы подхода через init:
 * - Меньше кода, нет промежуточного "builder".
 * - Валидация логически находится в том же классе, где и данные.
 * - Простая интеграция со Spring Boot.
 *
 * <p>
 * Простое наличие @ConfigurationProperties не создаёт бин само по себе.
 * Spring Boot с версии 2.2 и выше требует, чтобы класс с @ConfigurationProperties был либо:
 * - Помечен как компонент (@Component),
 * - Или зарегистрирован через @EnableConfigurationProperties(RestProperties::class),
 * - Или был возвращён как бин из конфигурации (@Bean).
 *
 * Иначе Spring просто не будет знать, что нужно создать бин для твоего класса.
 */
@ConfigurationProperties(prefix = "audit.client.rest")
data class RestProperties(
    override val enabled: Boolean? = null,
    val url: String,
    val circuitBreaker: CircuitBreakerConfiguration,
    val priority: Int = 10
) : EnablerProperties {

    init {
        require(url.isNotEmpty()) { "audit.client.rest.url обязателен для заполнения" }
        require(url.startsWith("http://") || url.startsWith("https://")) {
            "audit.client.rest.url не начинается с http или https"
        }
        //requireNotNull для circuitBreaker можно опустить, если сделать его не-nullable в конструкторе.
        requireNotNull(circuitBreaker) { "audit.client.rest.circuitBreaker обязателен для заполнения" }
    }
}


//implementation("org.springframework.boot:spring-boot-starter-validation")
//Что происходит:
// Можно использовать стандартные аннотации jakarta.validation (раньше javax.validation), а Spring Boot
// автоматически вызовет валидацию при биндинге свойств.
//
//@Validated включает валидацию для этого класса при создании бина.
//
//Аннотации (@NotBlank, @Pattern, @Valid) проверяются автоматически.
//
//Если данные некорректные — Spring выбрасывает BindValidationException при старте приложения.
//
//@Component гарантирует, что Spring создаст бин RestProperties.

//import jakarta.validation.constraints.NotBlank
//import jakarta.validation.constraints.Pattern
//import org.springframework.boot.context.properties.ConfigurationProperties
//import org.springframework.boot.context.properties.EnableConfigurationProperties
//import org.springframework.stereotype.Component
//import org.springframework.validation.annotation.Validated
//
//@Validated
//@ConfigurationProperties(prefix = "audit.client.rest")
//@Component
//data class RestProperties(
//    @field:NotBlank(message = "audit.client.rest.url обязателен для заполнения")
//    @field:Pattern(
//        regexp = "https?://.*",
//        message = "audit.client.rest.url должен начинаться с http или https"
//    )
//    val url: String,
//
//    @field:jakarta.validation.Valid
//    val circuitBreaker: CircuitBreakerConfiguration,
//
//    val priority: Int = 10,
//
//    val enabled: Boolean? = null
//) : EnablerProperties