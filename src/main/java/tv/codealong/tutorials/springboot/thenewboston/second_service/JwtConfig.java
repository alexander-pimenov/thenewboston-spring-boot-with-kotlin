package tv.codealong.tutorials.springboot.thenewboston.second_service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.security.Keys;

/**
 * NimbusJwtDecoder и JwtDecoder из библиотеки Spring Security OAuth2 Resource Server.
 * Они используются для работы с JWT токенами.
 * Краткое пояснение по JWT классам:
 * - JwtDecoder - интерфейс Spring Security для декодирования JWT токенов
 * - NimbusJwtDecoder - реализация от Nimbus JOSE + JWT библиотеки
 * - Keys - утилитный класс для создания ключей
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
@Validated
public class JwtConfig {

    @NotBlank
    private String secret;

    @Min(1)
    private long expirationMs;

    @NotBlank
    private String issuer;

    @NotNull
    private List<String> allowedAudiences;

    @Bean("jwtDecoder")
    public JwtDecoder jwtDecoder() {
        // Добавьте проверку на null
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    /**
     * Если нужна ручная работа с JWT (генерация токенов)
     * @return
     */
    @Bean("jwtDecoderManual")
    public JwtDecoder jwtDecoderManual() {
        // Создаем SecretKey без зависимости от jjwt
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // Используем SHA-256 для HMAC
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
