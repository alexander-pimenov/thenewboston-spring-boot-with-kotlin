package tv.codealong.tutorials.springboot.thenewboston.second_service;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проблема в том, что `ApplicationContextRunner` создает бин `JwtConfig`, но **не устанавливает свойства** перед вызовом `@Bean` методов. Свойство `secret` остаётся `null`.
 * <p>
 * Вот исправленные варианты:
 * <p>
 * ## **Решение 1: Используйте `@ConfigurationProperties` правильно**
 * <p>
 * ```java
 * package com.example.demo.config;
 *
 * import org.junit.jupiter.api.Test;
 * import org.springframework.boot.context.properties.EnableConfigurationProperties;
 * import org.springframework.boot.test.context.runner.ApplicationContextRunner;
 *
 * import static org.assertj.core.api.Assertions.assertThat;
 *
 * class JwtConfigContextTest {
 *
 * private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
 * .withUserConfiguration(TestConfig.class) // Используем конфигурацию
 * .withPropertyValues(
 * "app.jwt.secret=my-super-secret-key-that-is-at-least-32-characters-long",
 * "app.jwt.expiration-ms=86400000",
 * "app.jwt.issuer=demo-app",
 * "app.jwt.allowed-audiences=web,mobile"
 * );
 *
 * @Test
 * void shouldLoadProperties() {
 * contextRunner.run(context -> {
 * // Проверяем, что бин создан
 * assertThat(context).hasSingleBean(JwtConfig.class);
 *
 * JwtConfig config = context.getBean(JwtConfig.class);
 * assertThat(config.getSecret()).isEqualTo("my-super-secret-key-that-is-at-least-32-characters-long");
 * assertThat(config.getExpirationMs()).isEqualTo(86400000L);
 * assertThat(config.getIssuer()).isEqualTo("demo-app");
 * assertThat(config.getAllowedAudiences()).containsExactly("web", "mobile");
 * });
 * }
 *
 * @Test
 * void shouldCreateJwtDecoder() {
 * contextRunner.run(context -> {
 * // Проверяем, что декодер создается
 * assertThat(context).hasSingleBean(JwtConfig.class);
 * assertThat(context).hasSingleBean(JwtDecoder.class); // Проверяем декодер
 *
 * JwtDecoder decoder = context.getBean(JwtDecoder.class);
 * assertThat(decoder).isNotNull();
 * });
 * }
 *
 * // Тестовая конфигурация
 * @EnableConfigurationProperties(JwtConfig.class)
 * static class TestConfig {
 * // Пустая конфигурация, свойства загрузятся через @EnableConfigurationProperties
 * }
 * }
 * ```
 * <p>
 * ## **Решение 2: Исправьте JwtConfig.java**
 * <p>
 * Проблема в том, что `jwtDecoder()` вызывается **до** установки свойств. Добавьте проверку:
 * <p>
 * ```java
 * package com.example.demo.config;
 *
 * import org.springframework.boot.context.properties.ConfigurationProperties;
 * import org.springframework.context.annotation.Bean;
 * import org.springframework.context.annotation.Configuration;
 * import org.springframework.security.oauth2.jwt.JwtDecoder;
 * import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
 *
 * import javax.crypto.SecretKey;
 * import javax.crypto.spec.SecretKeySpec;
 * import java.nio.charset.StandardCharsets;
 * import java.util.List;
 *
 * @Configuration
 * @ConfigurationProperties(prefix = "app.jwt")
 * public class JwtConfig {
 *
 * private String secret;
 * private long expirationMs;
 * private String issuer;
 * private List<String> allowedAudiences;
 *
 * // Геттеры и сеттеры
 * public String getSecret() { return secret; }
 * public void setSecret(String secret) { this.secret = secret; }
 * public long getExpirationMs() { return expirationMs; }
 * public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
 * public String getIssuer() { return issuer; }
 * public void setIssuer(String issuer) { this.issuer = issuer; }
 * public List<String> getAllowedAudiences() { return allowedAudiences; }
 * public void setAllowedAudiences(List<String> allowedAudiences) { this.allowedAudiences = allowedAudiences; }
 *
 * @Bean
 * public JwtDecoder jwtDecoder() {
 * // Добавьте проверку на null
 * if (secret == null || secret.isEmpty()) {
 * throw new IllegalStateException("JWT secret is not configured");
 * }
 *
 * byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
 * SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
 * return NimbusJwtDecoder.withSecretKey(key).build();
 * }
 * }
 * ```
 * <p>
 * ## **Решение 3: Тест без вызова jwtDecoder()**
 * <p>
 * ```java
 * package com.example.demo.config;
 *
 * import org.junit.jupiter.api.Test;
 * import org.springframework.boot.context.properties.EnableConfigurationProperties;
 * import org.springframework.boot.test.context.runner.ApplicationContextRunner;
 * import org.springframework.context.annotation.Bean;
 * import org.springframework.context.annotation.Configuration;
 *
 * import static org.assertj.core.api.Assertions.assertThat;
 *
 * class JwtConfigPropertiesTest {
 *
 * private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
 * .withUserConfiguration(JwtConfigPropertiesOnly.class) // Только для свойств
 * .withPropertyValues(
 * "app.jwt.secret=test-secret-123456789012345678901234567890",
 * "app.jwt.expiration-ms=3600000",
 * "app.jwt.issuer=test-app",
 * "app.jwt.allowed-audiences=web"
 * );
 *
 * @Test
 * void shouldLoadProperties() {
 * contextRunner.run(context -> {
 * assertThat(context).hasSingleBean(JwtConfig.class);
 *
 * JwtConfig config = context.getBean(JwtConfig.class);
 * assertThat(config.getSecret()).isEqualTo("test-secret-123456789012345678901234567890");
 * assertThat(config.getExpirationMs()).isEqualTo(3600000L);
 * });
 * }
 *
 * // Конфигурация БЕЗ @Bean jwtDecoder
 * @EnableConfigurationProperties(JwtConfig.class)
 * static class JwtConfigPropertiesOnly {
 * // Только свойства, без создания декодера
 * }
 *
 * @Test
 * void testJwtDecoderSeparately() {
 * // Отдельный тест для jwtDecoder
 * JwtConfig config = new JwtConfig();
 * config.setSecret("test-secret-123456789012345678901234567890");
 *
 * // Проверяем, что метод не падает
 * assertThat(config.jwtDecoder()).isNotNull();
 * }
 * }
 * ```
 * <p>
 * ## **Решение 4: Разделите тесты - свойства и бины**
 * <p>
 * ```java
 * package com.example.demo.config;
 *
 * import org.junit.jupiter.api.Test;
 * import org.springframework.boot.context.properties.EnableConfigurationProperties;
 * import org.springframework.boot.test.context.runner.ApplicationContextRunner;
 * import org.springframework.context.annotation.Bean;
 * import org.springframework.context.annotation.Configuration;
 *
 * import static org.assertj.core.api.Assertions.assertThat;
 *
 * class JwtConfigTest {
 *
 * // Тест 1: Только свойства
 * @Test
 * void shouldLoadProperties() {
 * ApplicationContextRunner contextRunner = new ApplicationContextRunner()
 * .withUserConfiguration(PropertiesOnlyConfig.class)
 * .withPropertyValues(
 * "app.jwt.secret=secret123",
 * "app.jwt.expiration-ms=1000"
 * );
 *
 * contextRunner.run(context -> {
 * assertThat(context).hasSingleBean(JwtConfig.class);
 * JwtConfig config = context.getBean(JwtConfig.class);
 * assertThat(config.getSecret()).isEqualTo("secret123");
 * });
 * }
 *
 * // Тест 2: Полная конфигурация
 * @Test
 * void shouldCreateFullConfiguration() {
 * ApplicationContextRunner contextRunner = new ApplicationContextRunner()
 * .withUserConfiguration(FullConfig.class)
 * .withPropertyValues(
 * "app.jwt.secret=my-super-secret-key-that-is-at-least-32-characters-long",
 * "app.jwt.expiration-ms=86400000"
 * );
 *
 * contextRunner.run(context -> {
 * assertThat(context).hasSingleBean(JwtConfig.class);
 * assertThat(context).hasSingleBean(JwtDecoder.class);
 * });
 * }
 *
 * // Конфигурация только для свойств
 * @EnableConfigurationProperties(JwtConfig.class)
 * static class PropertiesOnlyConfig {
 * // Без @Bean методов
 * }
 *
 * // Полная конфигурация
 * @Configuration
 * @EnableConfigurationProperties(JwtConfig.class)
 * static class FullConfig extends JwtConfig {
 * // Наследуем от JwtConfig, чтобы получить @Bean методы
 * }
 * }
 * ```
 * <p>
 * ## **Решение 5: Самый простой рабочий тест**
 * <p>
 * ```java
 * package com.example.demo.config;
 *
 * import org.junit.jupiter.api.Test;
 * import org.springframework.boot.test.context.runner.ApplicationContextRunner;
 *
 * import java.util.List;
 *
 * import static org.assertj.core.api.Assertions.assertThat;
 *
 * class JwtConfigSimpleTest {
 *
 * @Test
 * void testJwtConfigWithoutDecoder() {
 * // Тестируем только заполнение свойств
 * new ApplicationContextRunner()
 * .withBean(JwtConfig.class)
 * .withPropertyValues(
 * "app.jwt.secret=test123",
 * "app.jwt.expiration-ms=5000"
 * )
 * .run(context -> {
 * JwtConfig config = context.getBean(JwtConfig.class);
 * assertThat(config.getSecret()).isEqualTo("test123");
 * assertThat(config.getExpirationMs()).isEqualTo(5000L);
 * });
 * }
 *
 * @Test
 * void testJwtDecoderManually() {
 * // Тестируем декодер отдельно
 * JwtConfig config = new JwtConfig();
 * config.setSecret("test-secret-123456789012345678901234567890");
 * config.setExpirationMs(3600000L);
 * config.setIssuer("test");
 * config.setAllowedAudiences(List.of("web"));
 *
 * // Вызываем метод вручную
 * var decoder = config.jwtDecoder();
 * assertThat(decoder).isNotNull();
 * }
 * }
 * ```
 * <p>
 * ## **Решение 6: Используйте @MockBean для зависимостей**
 * <p>
 * Если в `jwtDecoder()` есть другие зависимости:
 * <p>
 * ```java
 * package com.example.demo.config;
 *
 * import org.junit.jupiter.api.Test;
 * import org.springframework.boot.context.properties.EnableConfigurationProperties;
 * import org.springframework.boot.test.context.runner.ApplicationContextRunner;
 * import org.springframework.boot.test.mock.mockito.MockBean;
 * import org.springframework.context.annotation.Bean;
 * import org.springframework.context.annotation.Configuration;
 *
 * import static org.assertj.core.api.Assertions.assertThat;
 *
 * class JwtConfigWithMockTest {
 *
 * private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
 * .withUserConfiguration(JwtConfigTestConfig.class)
 * .withPropertyValues(
 * "app.jwt.secret=valid-secret-123456789012345678901234567890",
 * "app.jwt.expiration-ms=86400000"
 * );
 *
 * @Test
 * void testWithValidSecret() {
 * contextRunner.run(context -> {
 * assertThat(context).hasSingleBean(JwtConfig.class);
 * assertThat(context).hasSingleBean(JwtDecoder.class);
 * });
 * }
 *
 * @Configuration
 * @EnableConfigurationProperties(JwtConfig.class)
 * static class JwtConfigTestConfig extends JwtConfig {
 * // Наследуем, чтобы получить @Bean методы
 *
 * @Bean
 * @Override
 * public JwtDecoder jwtDecoder() {
 * // Можно переопределить, если нужно мокировать
 * return super.jwtDecoder();
 * }
 * }
 * }
 * ```
 * <p>
 * ## **Решение 7: Если ничего не помогает - тестируйте без Spring**
 * <p>
 * ```java
 * package com.example.demo.config;
 *
 * import org.junit.jupiter.api.Test;
 * import org.springframework.security.oauth2.jwt.JwtDecoder;
 *
 * import javax.crypto.SecretKey;
 * import javax.crypto.spec.SecretKeySpec;
 * import java.nio.charset.StandardCharsets;
 * import java.util.List;
 *
 * import static org.assertj.core.api.Assertions.assertThat;
 * import static org.assertj.core.api.Assertions.assertThatThrownBy;
 *
 * class JwtConfigUnitTest {
 *
 * @Test
 * void shouldCreateDecoderWithValidSecret() {
 * // Arrange
 * JwtConfig config = new JwtConfig();
 * config.setSecret("valid-secret-123456789012345678901234567890");
 *
 * // Act
 * JwtDecoder decoder = config.jwtDecoder();
 *
 * // Assert
 * assertThat(decoder).isNotNull();
 * }
 *
 * @Test
 * void shouldThrowExceptionWhenSecretIsNull() {
 * // Arrange
 * JwtConfig config = new JwtConfig();
 * config.setSecret(null);
 *
 * // Act & Assert
 * assertThatThrownBy(() -> config.jwtDecoder())
 * .isInstanceOf(IllegalStateException.class)
 * .hasMessageContaining("JWT secret is not configured");
 * }
 *
 * @Test
 * void shouldThrowExceptionWhenSecretIsEmpty() {
 * // Arrange
 * JwtConfig config = new JwtConfig();
 * config.setSecret("");
 *
 * // Act & Assert
 * assertThatThrownBy(() -> config.jwtDecoder())
 * .isInstanceOf(IllegalStateException.class)
 * .hasMessageContaining("JWT secret is not configured");
 * }
 *
 * @Test
 * void testSecretValidation() {
 * JwtConfig config = new JwtConfig();
 *
 * // Слишком короткий секрет
 * config.setSecret("short");
 * assertThatThrownBy(() -> config.jwtDecoder())
 * .isInstanceOfAny(IllegalArgumentException.class, IllegalStateException.class);
 * }
 * }
 * ```
 * <p>
 * ## **Ключевая проблема и решение:**
 * <p>
 * **Проблема**: Spring создает `JwtConfig` → вызывает `@Bean` метод `jwtDecoder()` → `secret` ещё `null`.
 * <p>
 * **Решение**: Добавьте проверку в `jwtDecoder()`:
 * <p>
 * ```java
 * @Bean
 * public JwtDecoder jwtDecoder() {
 * // ВАЖНО: проверка перед использованием secret
 * if (secret == null || secret.trim().isEmpty()) {
 * throw new IllegalStateException("JWT secret must be configured");
 * }
 *
 * // Остальной код...
 * }
 * ```
 * <p>
 * ## **Рекомендация:**
 * <p>
 * Используйте **Решение 7 (unit-тест без Spring)** для тестирования логики и **Решение 1 (с TestConfig)** для интеграционного тестирования.
 */
// Самый простой тест - без Spring вообще
class JwtConfigSimpleTest {

    @Test
    void testJwtConfigCreation() {
        // Arrange
        JwtConfig config = new JwtConfig();
        config.setSecret("test-secret-key-123456789012345678901234567890");
        config.setExpirationMs(3600000L);
        config.setIssuer("test-app");
        config.setAllowedAudiences(List.of("web"));

        // Act
        JwtDecoder decoder = config.jwtDecoder();

        // Assert
        assertThat(config.getSecret()).isNotEmpty();
        assertThat(config.getExpirationMs()).isPositive();
        assertThat(decoder).isNotNull();
    }

    @Test
    void testJwtConfigWithProperties() {
        // Тест с @ConfigurationProperties
        JwtConfig config = new JwtConfig();

        // Симулируем установку свойств Spring
        config.setSecret("secret123456789012345678901234567890");
        config.setExpirationMs(86400000L);
        config.setIssuer("my-app");
        config.setAllowedAudiences(List.of("web", "mobile", "api"));

        assertThat(config.getSecret()).hasSizeGreaterThanOrEqualTo(32);
        assertThat(config.getAllowedAudiences()).hasSize(3);
    }
}
