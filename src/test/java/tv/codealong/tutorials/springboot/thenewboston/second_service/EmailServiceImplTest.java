package tv.codealong.tutorials.springboot.thenewboston.second_service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Отличный вопрос! Вы уже хорошо разбираетесь в Mockito и Spring-тестировании, и сейчас самое время чётко разграничить, **когда использовать `@Mock`, `@InjectMocks`, `@MockBean` и ручное создание объектов**.
 *
 * ---
 *
 * ## 🧩 Краткий ответ
 *
 * | Аннотация / способ | Когда использовать |
 * |-------------------|-------------------|
 * | `@Mock` | Для создания мока (заглушки) зависимости **в юнит-тестах с Mockito** |
 * | `@InjectMocks` | Чтобы Mockito **автоматически внедрил** `@Mock`-объекты в тестируемый класс (не всегда надёжно) |
 * | `@MockBean` | В **интеграционных тестах на Spring Context** — заменяет бин в контексте на мок |
 * | **Ручное создание** | Когда нужен полный контроль, особенно с параметрами конструктора или при сложных зависимостях |
 *
 * ---
 *
 * Теперь разберём подробно, с примерами из вашего кода.
 *
 * ---
 *
 * ## 1. `@Mock` — создаёт мок объекта
 *
 * ### ✅ Когда использовать:
 * - Когда вам нужна заглушка для зависимости (например, `KafkaTemplate`, `RestTemplate`, `UserService` и т.п.).
 * - В **юнит-тестах**, где вы тестируете **один класс изолированно**.
 *
 * ### ✅ Пример:
 * ```java src/test/java/tv/codealong/tutorials/springboot/thenewboston/second_service/EmailServiceImplTest.java
 * @Mock
 * private KafkaTemplate<String, EmailMessage> emailKafkaTemplate;
 * ```
 *
 * > Это создаёт "пустышку" `KafkaTemplate`, которую вы можете настроить через `when(...).thenReturn(...)`.
 *
 * ---
 *
 * ## 2. `@InjectMocks` — пробует внедрить моки в целевой объект
 *
 * ### ✅ Когда использовать:
 * - Когда тестируемый класс имеет **конструктор с зависимостями**, и вы хотите, чтобы Mockito **сам собрал объект**.
 *
 * ### ⚠️ Проблемы:
 * - Работает **не всегда**: если несколько конструкторов, поля `@Value`, `@Lazy` и т.п. — может сломаться.
 * - **Не работает с `@Value` полями** — как у вас (`emailTopic`, `notificationTopic`) — Mockito не может их заполнить.
 *
 * ### ❌ У вас сейчас:
 * ```java
 * @InjectMocks
 * private EmailServiceImpl emailService;
 * ```
 *
 * > Это **не сработает**, если вы оставите `@Value("${...}")` — поля останутся `null`.
 *
 * ---
 *
 * ## 3. `@MockBean` — заменяет бин в Spring Context
 *
 * ### ✅ Когда использовать:
 * - В **интеграционных тестах** (`@SpringBootTest`), где запускается Spring-контекст.
 * - Когда хотите **замокать сервис, репозиторий или KafkaTemplate** в реальном приложении.
 *
 * ### 🌰 Пример:
 * ```java
 * @SpringBootTest
 * @MockBean
 * KafkaTemplate<String, EmailMessage> kafkaTemplate; // заменит настоящий бин
 *
 * @Test
 * void testSendEmail() {
 *     // kafkaTemplate будет моком во всём контексте
 * }
 * ```
 *
 * > 🔥 `@MockBean` — это **Spring**, а не чистый Mockito.
 *
 * ---
 *
 * ## 4. Ручное создание объекта — полный контроль
 *
 * ### ✅ Когда использовать:
 * - Когда нужно передать **параметры конструктора** (например, `@Value`).
 * - Когда `@InjectMocks` **не справляется**.
 * - Когда вы хотите **явно контролировать**, как создаётся тестируемый класс.
 *
 * ### ✅ Ваш текущий подход — **правильный**:
 * ```java src/test/java/tv/codealong/tutorials/springboot/thenewboston/second_service/EmailServiceImplTest.java
 * @BeforeEach
 * void setUp() {
 *     emailService = new EmailServiceImpl(emailKafkaTemplate, EMAIL_TOPIC, NOTIFICATION_TOPIC);
 *     ReflectionTestUtils.setField(emailService, "emailTopic", EMAIL_TOPIC);
 *     ReflectionTestUtils.setField(emailService, "notificationTopic", NOTIFICATION_TOPIC);
 * }
 * ```
 *
 * > Лучше **передавать через конструктор** — тогда `ReflectionTestUtils` не нужен.
 *
 * ---
 *
 * ## ✅ Что выбрать? Рекомендации
 *
 * | Сценарий | Что использовать |
 * |--------|------------------|
 * | **Юнит-тест (изолированный класс)** | `@Mock` + **ручное создание** объекта |
 * | **Юнит-тест, простой бин без `@Value`** | `@Mock` + `@InjectMocks` |
 * | **Интеграционный тест (`@SpringBootTest`)** | `@MockBean` |
 * | **Тест с `@Value`, `@Qualifier`, сложной внедряемостью** | **Ручное создание** + конструктор/сеттеры |
 * | **Приватные методы, поля — тест через рефлексию** | `ReflectionTestUtils.invokeMethod()` или `setField()` |
 *
 * ---
 *
 * ## 🔁 Как улучшить ваш код
 *
 * ### ✅ Измените `EmailServiceImpl`:
 * ```java src/main/java/tv/codealong/tutorials/springboot/thenewboston/second_service/EmailServiceImpl.java
 * @Service
 * public class EmailServiceImpl implements EmailService {
 *
 *     private final KafkaTemplate<String, EmailMessage> emailKafkaTemplate;
 *     private String emailTopic;
 *     private String notificationTopic;
 *
 *     public EmailServiceImpl(KafkaTemplate<String, EmailMessage> emailKafkaTemplate,
 *                             @Value("${kafka.topics.email}") String emailTopic,
 *                             @Value("${kafka.topics.notification}") String notificationTopic) {
 *         this.emailKafkaTemplate = emailKafkaTemplate;
 *         this.emailTopic = emailTopic;
 *         this.notificationTopic = notificationTopic;
 *     }
 *
 *     // ... остальной код
 * }
 * ```
 *
 * ### ✅ Тогда в тесте:
 * ```java src/test/java/tv/codealong/tutorials/springboot/thenewboston/second_service/EmailServiceImplTest.java
 * @BeforeEach
 * void setUp() {
 *     emailService = new EmailServiceImpl(emailKafkaTemplate, EMAIL_TOPIC, NOTIFICATION_TOPIC);
 * }
 * ```
 *
 * > Уберите `@InjectMocks`, `ReflectionTestUtils` — всё будет чище.
 *
 * ---
 *
 * ## 📌 Итог
 *
 * - `@Mock` — для мока зависимости в юнит-тестах.
 * - `@InjectMocks` — удобен, но **ненадёжен** при сложных зависимостях.
 * - `@MockBean` — только в **интеграционных тестах** с Spring.
 * - **Ручное создание** — самый надёжный способ, особенно с `@Value`.
 *
 * > ✅ Вы уже на правильном пути — переход на конструктор-инъекцию сделает тесты ещё лучше.
 *
 * Если хотите — могу переписать весь тест с улучшениями. Напишите: *"Перепиши с улучшениями"*.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private KafkaTemplate<String, EmailMessage> emailKafkaTemplate;

    //Уберите @InjectMocks, если используете ручное создание экземпляра.
    //@InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<EmailMessage> emailMessageCaptor;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    private static final String EMAIL_TOPIC = "email-events";
    private static final String NOTIFICATION_TOPIC = "notification-events";

    @BeforeEach
    void setUp() {
        // Используем рефлексию для установки значений полей
        emailService = new EmailServiceImpl(emailKafkaTemplate, EMAIL_TOPIC, NOTIFICATION_TOPIC);

        //1. Настройка полей через рефлексию — не лучшая практика
        //Проблема: Это хрупко, нечитаемо и легко сломать при рефакторинге (например, переименование поля).
        //Решение: Передавайте значения через конструктор или сеттеры.
        // Устанавливаем значения через reflection или setter-методы
//        try {
//            var topicField = EmailServiceImpl.class.getDeclaredField("emailTopic");
//            topicField.setAccessible(true);
//            topicField.set(emailService, EMAIL_TOPIC);
//
//            var notificationTopicField = EmailServiceImpl.class.getDeclaredField("notificationTopic");
//            notificationTopicField.setAccessible(true);
//            notificationTopicField.set(emailService, NOTIFICATION_TOPIC);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        // Но можно и так: Устанавливаем значения полей через ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "emailTopic", EMAIL_TOPIC);
        ReflectionTestUtils.setField(emailService, "notificationTopic", NOTIFICATION_TOPIC);
    }

    @Test
    void sendWelcomeEmail_shouldSendToKafka() {
        // Arrange
        String email = "test@example.com";
        // Создаем успешный CompletableFuture
        SendResult<String, EmailMessage> sendResult = mock(SendResult.class);

        CompletableFuture<SendResult<String, EmailMessage>> future =
                CompletableFuture.completedFuture(sendResult);

        when(emailKafkaTemplate.send(eq(EMAIL_TOPIC), anyString(), any(EmailMessage.class)))
                .thenReturn(future);
        // Act
        emailService.sendWelcomeEmail(email);

        // Небольшая пауза для async
        //. Thread.sleep(100) — нестабильное решение
        //Проблема: Это хардкод, зависит от скорости выполнения, может не сработать на медленной машине.
        //Решение: Используйте verify(..., timeout(...)) — вы уже используете его в другом тесте!
        //✅ Исправьте первый тест:
        //Уберите Thread.sleep и замените times(1) на timeout(1000).times(1):
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//        }

        // Assert
        verify(emailKafkaTemplate, timeout(1000).times(1))
                .send(eq(EMAIL_TOPIC), anyString(), emailMessageCaptor.capture());

        verify(emailKafkaTemplate,  timeout(1000).times(1))
                .send(topicCaptor.capture(), keyCaptor.capture(), emailMessageCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(EMAIL_TOPIC);

        EmailMessage captured = emailMessageCaptor.getValue();
        assertThat(captured.getTo()).isEqualTo(email);
        assertThat(captured.getType()).isEqualTo("WELCOME");
        assertThat(captured.getSubject()).contains("Добро пожаловать");
    }

    @Test
    void sendPasswordResetEmail_shouldSendToKafka() {
        // Arrange
        String email = "user@example.com";
        String token = "reset-token-123";

        SendResult<String, EmailMessage> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, EmailMessage>> future =
                CompletableFuture.completedFuture(sendResult);

        when(emailKafkaTemplate.send(eq(EMAIL_TOPIC), anyString(), any(EmailMessage.class)))
                .thenReturn(future);

        // Act
        emailService.sendPasswordResetEmail(email, token);

        // Assert
        verify(emailKafkaTemplate, timeout(1000).times(1))
                .send(eq(EMAIL_TOPIC), anyString(), any(EmailMessage.class));
    }

    @Test
    void sendNotification_shouldSendToNotificationTopic() {
        // Arrange
        String email = "user@example.com";
        String subject = "Test Subject";
        String message = "Test Message";

        SendResult<String, EmailMessage> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, EmailMessage>> future =
                CompletableFuture.completedFuture(sendResult);

        when(emailKafkaTemplate.send(eq(NOTIFICATION_TOPIC), anyString(), any(EmailMessage.class)))
                .thenReturn(future);

        // Act
        emailService.sendNotification(email, subject, message);

        // Assert
        verify(emailKafkaTemplate, timeout(1000).times(1))
                .send(eq(NOTIFICATION_TOPIC), anyString(), any(EmailMessage.class));
    }

    @Test
    void sendWelcomeEmail_shouldHandleKafkaError() {
        // Arrange
        String email = "test@example.com";

        // Создаем неуспешный CompletableFuture
        CompletableFuture<SendResult<String, EmailMessage>> future =
                CompletableFuture.failedFuture(new RuntimeException("Kafka error"));

        when(emailKafkaTemplate.send(eq(EMAIL_TOPIC), anyString(), any(EmailMessage.class)))
                .thenReturn(future);

        // Act
        // Метод должен не бросать исключение, а только логировать ошибку
        emailService.sendWelcomeEmail(email);

        // Assert - проверяем, что метод send был вызван
        verify(emailKafkaTemplate, times(1))
                .send(anyString(), anyString(), any(EmailMessage.class));
    }

    @Test
    void extractUsernameFromEmail_shouldExtractCorrectly() {
        // Используем рефлексию для тестирования приватного метода
        String username = (String) ReflectionTestUtils.invokeMethod(
                emailService, "extractUsernameFromEmail", "john.doe@example.com");

        assertThat(username).isEqualTo("john.doe");

        // Для email без @
        String invalid = (String) ReflectionTestUtils.invokeMethod(
                emailService, "extractUsernameFromEmail", "invalid-email");

        assertThat(invalid).isEqualTo("Пользователь");

        // Для null
        String nullResult = (String) ReflectionTestUtils.invokeMethod(
                emailService, "extractUsernameFromEmail", (String) null);

        assertThat(nullResult).isEqualTo("Пользователь");
    }
}