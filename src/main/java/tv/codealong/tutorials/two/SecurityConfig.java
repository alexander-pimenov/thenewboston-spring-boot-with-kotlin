package tv.codealong.tutorials.two;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder Configuration
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt - самый распространенный и безопасный вариант
        return new BCryptPasswordEncoder();

        // Альтернативные варианты:
        // return NoOpPasswordEncoder.getInstance(); // ТОЛЬКО для тестов!
        // return new Pbkdf2PasswordEncoder(); // Более новый алгоритм
        // return new Argon2PasswordEncoder(); // Самый современный
    }
}