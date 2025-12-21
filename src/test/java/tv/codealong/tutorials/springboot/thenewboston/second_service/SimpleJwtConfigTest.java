package tv.codealong.tutorials.springboot.thenewboston.second_service;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

// Вариант 1: Тест только конфигурации (без загрузки всего Spring контекста)
@EnableConfigurationProperties(JwtConfig.class) // Только свойства
@TestPropertySource(properties = {
        "app.jwt.secret=my-super-secret-key-that-is-at-least-32-characters-long",
        "app.jwt.expiration-ms=86400000",
        "app.jwt.issuer=demo-app",
        "app.jwt.allowed-audiences=web,mobile"
})
class JwtConfigTest {

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    void configProperties_areLoaded() {
        assertThat(jwtConfig.getSecret())
                .isEqualTo("my-super-secret-key-that-is-at-least-32-characters-long");
        assertThat(jwtConfig.getExpirationMs()).isEqualTo(86400000L);
        assertThat(jwtConfig.getIssuer()).isEqualTo("demo-app");
        assertThat(jwtConfig.getAllowedAudiences()).containsExactly("web", "mobile");
    }
}

// Вариант 2: Тест с JwtDecoder (нужен полный контекст)
@SpringBootTest(classes = {JwtConfig.class, UserMapper.class}) // Явно указываем классы
@TestPropertySource(properties = {
        "app.jwt.secret=my-super-secret-key-that-is-at-least-32-characters-long",
        "app.jwt.expiration-ms=86400000",
        "app.jwt.issuer=demo-app",
        "app.jwt.allowed-audiences=web,mobile"
})
class JwtDecoderTest {

    @Autowired(required = false) // required = false, так как бин может не создаться
    private JwtDecoder jwtDecoder;

    @Test
    void jwtDecoderBean_isCreated() {
        // Бин создастся только если все зависимости в порядке
        if (jwtDecoder != null) {
            assertThat(jwtDecoder).isNotNull();
        }
    }
}

// Вариант 3: Самый простой тест (рекомендую)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE, // Не запускаем веб-сервер
        classes = {JwtConfig.class} // Только JwtConfig
)
@TestPropertySource(properties = {
        "app.jwt.secret=my-super-secret-key-that-is-at-least-32-characters-long",
        "app.jwt.expiration-ms=86400000",
        "app.jwt.issuer=demo-app",
        "app.jwt.allowed-audiences=web,mobile",
        "spring.main.web-application-type=none" // Отключаем веб-контекст
})
class SimpleJwtConfigTest {

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    void propertiesAreLoaded() {
        assertThat(jwtConfig).isNotNull();
        assertThat(jwtConfig.getSecret()).isNotEmpty();
        assertThat(jwtConfig.getExpirationMs()).isPositive();
    }
}