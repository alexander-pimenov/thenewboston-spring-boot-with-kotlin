package tv.codealong.tutorials.two;


public interface EmailService {
    void sendWelcomeEmail(String email);
    void sendPasswordResetEmail(String email, String token);
    void sendNotification(String email, String subject, String message);
}