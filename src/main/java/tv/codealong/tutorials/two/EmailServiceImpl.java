package tv.codealong.tutorials.two;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendWelcomeEmail(String email) {
        log.info("Sending welcome email to: {}", email);
        // Реальная реализация отправки email
        // mailSender.send(...)
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        log.info("Sending password reset email to: {} with token: {}", email, token);
    }

    @Override
    public void sendNotification(String email, String subject, String message) {
        log.info("Sending notification to: {}, subject: {}, message: {}", email, subject, message);
    }
}