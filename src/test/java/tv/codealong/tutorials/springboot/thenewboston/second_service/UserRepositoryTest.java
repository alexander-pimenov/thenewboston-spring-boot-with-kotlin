package tv.codealong.tutorials.springboot.thenewboston.second_service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Вы правы! `TestEntityManager` из `@DataJpaTest` не имеет метода `createNativeQuery`. Вот правильные способы:
 *
 * ## **1. Используйте обычный EntityManager (он есть внутри TestEntityManager)**
 *
 * ```java
 * @Autowired
 * private TestEntityManager entityManager;
 *
 * @Test
 * void testWithNativeQuery() {
 *     // Получаем EntityManager из TestEntityManager
 *     EntityManager em = entityManager.getEntityManager();
 *
 *     // Теперь можно использовать createNativeQuery
 *     em.createNativeQuery(
 *             "INSERT INTO users (email, password_hash, status, created_at, updated_at) " +
 *             "VALUES ('user1@example.com', 'encoded', 'ACTIVE', '2024-01-15 10:00:00', '2024-01-15 10:00:00')")
 *             .executeUpdate();
 *
 *     em.createNativeQuery(
 *             "INSERT INTO users (email, password_hash, status, created_at, updated_at) " +
 *             "VALUES ('user2@example.com', 'encoded', 'ACTIVE', '2024-02-01 10:00:00', '2024-02-01 10:00:00')")
 *             .executeUpdate();
 *
 *     em.flush();
 *
 *     // Дальше ваш тест...
 * }
 * ```
 *
 * ## **2. Используйте JdbcTemplate (рекомендуется)**
 *
 * ```java
 * import org.springframework.jdbc.core.JdbcTemplate;
 *
 * @DataJpaTest
 * @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
 * class UserRepositoryTest {
 *
 *     @Autowired
 *     private UserRepository userRepository;
 *
 *     @Autowired
 *     private TestEntityManager entityManager;
 *
 *     @Autowired
 *     private JdbcTemplate jdbcTemplate; // Spring Boot автоматически настраивает
 *
 *     @Test
 *     @DisplayName("findByCreatedAtBetween_returnsUsersInDateRange - с JdbcTemplate")
 *     void findByCreatedAtBetween_returnsUsersInDateRange_withJdbcTemplate() {
 *         // Arrange
 *         LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
 *         LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59, 59, 999999999);
 *
 *         // Вставляем через JdbcTemplate
 *         jdbcTemplate.update(
 *             "INSERT INTO users (email, password_hash, status, created_at, updated_at) " +
 *             "VALUES (?, ?, ?, ?, ?)",
 *             "user1@example.com", "encodedPassword", "ACTIVE",
 *             LocalDateTime.of(2024, 1, 15, 10, 0),
 *             LocalDateTime.of(2024, 1, 15, 10, 0)
 *         );
 *
 *         jdbcTemplate.update(
 *             "INSERT INTO users (email, password_hash, status, created_at, updated_at) " +
 *             "VALUES (?, ?, ?, ?, ?)",
 *             "user2@example.com", "encodedPassword", "ACTIVE",
 *             LocalDateTime.of(2024, 2, 1, 10, 0),
 *             LocalDateTime.of(2024, 2, 1, 10, 0)
 *         );
 *
 *         // Act
 *         List<User> users = userRepository.findByCreatedAtBetween(startDate, endDate);
 *
 *         // Assert
 *         assertThat(users)
 *                 .hasSize(1)
 *                 .extracting(User::getEmail)
 *                 .containsExactly("user1@example.com");
 *     }
 * }
 * ```
 *
 * ## **3. Используйте SQL скрипты**
 *
 * ### **Создайте файл `src/test/resources/data.sql`:**
 * ```sql
 * INSERT INTO users (email, password_hash, status, created_at, updated_at)
 * VALUES ('user1@example.com', 'encoded', 'ACTIVE', '2024-01-15 10:00:00', '2024-01-15 10:00:00');
 *
 * INSERT INTO users (email, password_hash, status, created_at, updated_at)
 * VALUES ('user2@example.com', 'encoded', 'ACTIVE', '2024-02-01 10:00:00', '2024-02-01 10:00:00');
 * ```
 *
 * ### **Тест с @Sql:**
 * ```java
 * @Test
 * @Sql(scripts = "/data.sql") // Загружает SQL перед тестом
 * @Sql(scripts = "/cleanup.sql", executionPhase = AFTER_TEST_METHOD) // Очищает после
 * @DisplayName("findByCreatedAtBetween_returnsUsersInDateRange - с SQL скриптом")
 * void findByCreatedAtBetween_returnsUsersInDateRange_withSqlScript() {
 *     // Arrange
 *     LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
 *     LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59, 59, 999999999);
 *
 *     // Act
 *     List<User> users = userRepository.findByCreatedAtBetween(startDate, endDate);
 *
 *     // Assert
 *     assertThat(users)
 *             .hasSize(1)
 *             .extracting(User::getEmail)
 *             .containsExactly("user1@example.com");
 * }
 * ```
 *
 * ## **4. Используйте EntityManager.merge() вместо persist()**
 *
 * ```java
 * @Test
 * @DisplayName("findByCreatedAtBetween_returnsUsersInDateRange - с merge")
 * void findByCreatedAtBetween_returnsUsersInDateRange_withMerge() {
 *     // Arrange
 *     LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
 *     LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59, 59, 999999999);
 *
 *     EntityManager em = entityManager.getEntityManager();
 *
 *     User user1 = User.builder()
 *             .email("user1@example.com")
 *             .passwordHash("encodedPassword")
 *             .createdAt(LocalDateTime.of(2024, 1, 15, 10, 0))
 *             .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 0))
 *             .build();
 *
 *     User user2 = User.builder()
 *             .email("user2@example.com")
 *             .passwordHash("encodedPassword")
 *             .createdAt(LocalDateTime.of(2024, 2, 1, 10, 0))
 *             .updatedAt(LocalDateTime.of(2024, 2, 1, 10, 0))
 *             .build();
 *
 *     // Используем merge вместо persist
 *     em.merge(user1);
 *     em.merge(user2);
 *     em.flush();
 *     em.clear(); // Очищаем кэш
 *
 *     // Act
 *     List<User> users = userRepository.findByCreatedAtBetween(startDate, endDate);
 *
 *     // Assert
 *     assertThat(users)
 *             .hasSize(1)
 *             .extracting(User::getEmail)
 *             .containsExactly("user1@example.com");
 * }
 * ```
 *
 * ## **5. Сохраняйте через repository.save() с явными датами**
 *
 * ```java
 * @Test
 * @DisplayName("findByCreatedAtBetween_returnsUsersInDateRange - самый простой")
 * void findByCreatedAtBetween_returnsUsersInDateRange_simple() {
 *     // Arrange
 *     LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
 *     LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59, 59, 999999999);
 *
 *     // Создаем пользователей с явными датами
 *     User user1 = new User();
 *     user1.setEmail("user1@example.com");
 *     user1.setPasswordHash("encodedPassword");
 *     user1.setStatus(UserStatus.ACTIVE);
 *     user1.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
 *     user1.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
 *
 *     User user2 = new User();
 *     user2.setEmail("user2@example.com");
 *     user2.setPasswordHash("encodedPassword");
 *     user2.setStatus(UserStatus.ACTIVE);
 *     user2.setCreatedAt(LocalDateTime.of(2024, 2, 1, 10, 0));
 *     user2.setUpdatedAt(LocalDateTime.of(2024, 2, 1, 10, 0));
 *
 *     // Сохраняем через репозиторий
 *     userRepository.save(user1);
 *     userRepository.save(user2);
 *
 *     // Act
 *     List<User> users = userRepository.findByCreatedAtBetween(startDate, endDate);
 *
 *     // Assert
 *     assertThat(users)
 *             .hasSize(1)
 *             .extracting(User::getEmail)
 *             .containsExactly("user1@example.com");
 * }
 * ```
 *
 * ## **6. Временное отключение автоматических timestamp**
 *
 * ### **Измените User entity:**
 * ```java
 * @Entity
 * @Table(name = "users")
 * @Getter
 * @Setter
 * @NoArgsConstructor
 * @AllArgsConstructor
 * @Builder
 * @ToString
 * public class User {
 *
 *     @Id
 *     @GeneratedValue(strategy = GenerationType.IDENTITY)
 *     private Long id;
 *
 *     @Column(unique = true, nullable = false)
 *     private String email;
 *
 *     @Column(name = "password_hash", nullable = false)
 *     private String passwordHash;
 *
 *     @Enumerated(EnumType.STRING)
 *     @Column(nullable = false)
 *     @Builder.Default
 *     private UserStatus status = UserStatus.ACTIVE;
 *
 *     @Column(name = "created_at", updatable = false)
 *     @Builder.Default // Lombok установит значение при построении
 *     private LocalDateTime createdAt = null;
 *
 *     @Column(name = "updated_at")
 *     @Builder.Default
 *     private LocalDateTime updatedAt = null;
 *
 *     @PrePersist
 *     protected void onCreate() {
 *         // Устанавливаем timestamp только если не задан явно
 *         if (this.createdAt == null) {
 *             this.createdAt = LocalDateTime.now();
 *         }
 *         if (this.updatedAt == null) {
 *             this.updatedAt = LocalDateTime.now();
 *         }
 *     }
 *
 *     @PreUpdate
 *     protected void onUpdate() {
 *         this.updatedAt = LocalDateTime.now();
 *     }
 * }
 * ```
 *
 * ## **7. Полный пример рабочего теста:**
 *
 * ```java
 * @DataJpaTest
 * @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
 * class UserRepositoryTest {
 *
 *     @Autowired
 *     private UserRepository userRepository;
 *
 *     @Autowired
 *     private TestEntityManager entityManager;
 *
 *     @Autowired
 *     private JdbcTemplate jdbcTemplate;
 *
 *     @Test
 *     @DisplayName("findByCreatedAtBetween - JdbcTemplate вариант")
 *     void findByCreatedAtBetween_withJdbcTemplate() {
 *         // Arrange
 *         LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
 *         LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59, 59, 999999999);
 *
 *         // Вставляем данные
 *         insertTestData();
 *
 *         // Act
 *         List<User> users = userRepository.findByCreatedAtBetween(startDate, endDate);
 *
 *         // Assert
 *         assertThat(users)
 *                 .hasSize(1)
 *                 .extracting(User::getEmail)
 *                 .containsExactly("user1@example.com");
 *     }
 *
 *     private void insertTestData() {
 *         jdbcTemplate.update("DELETE FROM users"); // Очищаем перед вставкой
 *
 *         jdbcTemplate.update(
 *             "INSERT INTO users (email, password_hash, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
 *             "user1@example.com", "encoded1", "ACTIVE",
 *             LocalDateTime.of(2024, 1, 15, 10, 0),
 *             LocalDateTime.of(2024, 1, 15, 10, 0)
 *         );
 *
 *         jdbcTemplate.update(
 *             "INSERT INTO users (email, password_hash, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
 *             "user2@example.com", "encoded2", "ACTIVE",
 *             LocalDateTime.of(2024, 2, 1, 10, 0),
 *             LocalDateTime.of(2024, 2, 1, 10, 0)
 *         );
 *     }
 *
 *     @AfterEach
 *     void tearDown() {
 *         // Очистка после каждого теста
 *         jdbcTemplate.update("DELETE FROM users");
 *     }
 * }
 * ```
 *
 * ## **Рекомендации:**
 *
 * 1. **Для простоты**: Используйте `repository.save()` с явно заданными датами
 * 2. **Для контроля**: Используйте `JdbcTemplate` (самый надежный)
 * 3. **Для сложных данных**: Используйте `@Sql` скрипты
 * 4. **Для отладки**: Проверьте, что в `User` entity нет `@CreationTimestamp`/`@UpdateTimestamp`
 *
 * ## **Проверка:**
 * ```java
 * // Добавьте в тест для отладки
 * @Test
 * void debugTimestampBehavior() {
 *     User user = User.builder()
 *             .email("test@example.com")
 *             .passwordHash("hash")
 *             .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
 *             .updatedAt(LocalDateTime.of(2024, 1, 1, 0, 0))
 *             .build();
 *
 *     userRepository.save(user);
 *
 *     User saved = userRepository.findById(user.getId()).orElseThrow();
 *     System.out.println("Saved createdAt: " + saved.getCreatedAt());
 *     System.out.println("Saved updatedAt: " + saved.getUpdatedAt());
 *
 *     // Если даты изменились - проблема в аннотациях Hibernate
 * }
 * ```
 *
 * **JdbcTemplate** - лучший выбор для точного контроля данных в тестах репозиториев.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig.class)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByEmail_withExistingEmail_returnsUser")
    void findByEmail_withExistingEmail_returnsUser() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder()
                .email(email)
                .passwordHash("hash")
                .build();
        entityManager.persistAndFlush(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail(email);

        // Assert
        org.assertj.core.api.AssertionsForClassTypes.assertThat(foundUser)
                .isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo(email);
    }

    @Test
    @DisplayName("existsByEmail_withExistingEmail_returnsTrue")
    void existsByEmail_withExistingEmail_returnsTrue() {
        // Arrange
        String email = "existing@example.com";
        User user = User.builder()
                .email(email)
                .passwordHash("hash")
                .build();
        entityManager.persistAndFlush(user);

        // Act
        boolean exists = userRepository.existsByEmail(email);

        // Assert
        org.assertj.core.api.AssertionsForClassTypes.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findByStatus_withActiveUsers_returnsList")
    void findByStatus_withActiveUsers_returnsList() {
        // Arrange
        User activeUser = User.builder()
                .email("active@example.com")
                .status(UserStatus.ACTIVE)
                .passwordHash("encodedPassword")
                .build();
        User inactiveUser = User.builder()
                .email("inactive@example.com")
                .status(UserStatus.INACTIVE)
                .passwordHash("encodedPassword")
                .build();

        entityManager.persist(activeUser);
        entityManager.persist(inactiveUser);
        entityManager.flush();

        // Act
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);

        // Assert
        assertThat(activeUsers)
                .hasSize(1)
                .allMatch(user -> user.getStatus() == UserStatus.ACTIVE);
    }

    /**
     * Проблема в том, что у вас в сущности User есть @CreationTimestamp и @UpdateTimestamp аннотации Hibernate,
     * которые автоматически перезаписывают ваши значения при сохранении.
     */
    @Test
    @DisplayName("findByCreatedAtBetween_returnsUsersInDateRange")
    void findByCreatedAtBetween_returnsUsersInDateRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59, 59, 999);
        // или
        //LocalDateTime endDate = LocalDateTime.of(2024, 2, 1, 0, 0).minusNanos(1);

        User user1 = User.builder()
                .email("user1@example.com")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 0))
                .passwordHash("encodedPassword")
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .createdAt(LocalDateTime.of(2024, 2, 1, 10, 0)) // Outside range
                .updatedAt(LocalDateTime.of(2024, 2, 1, 10, 0)) // Outside range
                .passwordHash("encodedPassword")
                .build();

        System.out.println(user1);
        System.out.println(user2);

        // Вставляем напрямую через SQL чтобы hibernate не подставлял текущее время.
        EntityManager em = entityManager.getEntityManager();

        em.createNativeQuery(
                        "INSERT INTO users (email, password_hash, status, created_at, updated_at) " +
                                "VALUES ('user1@example.com', 'encoded', 'ACTIVE', '2024-01-15 10:00:00', '2024-01-15 10:00:00')")
                .executeUpdate();

        em.createNativeQuery(
                        "INSERT INTO users (email, password_hash, status, created_at, updated_at) " +
                                "VALUES ('user2@example.com', 'encoded', 'ACTIVE', '2024-02-01 10:00:00', '2024-02-01 10:00:00')")
                .executeUpdate();

        em.flush();

        // Act
        List<User> users = userRepository.findByCreatedAtBetween(startDate, endDate);

        // Assert
        assertThat(users)
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly("user1@example.com");
    }

    // Добавьте в тест для отладки
    @Test
    void debugTimestampBehavior() {
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hash")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();

        userRepository.save(user);

        User saved = userRepository.findById(user.getId()).orElseThrow();
        System.out.println("Saved createdAt: " + saved.getCreatedAt()); //Saved createdAt: 2025-12-21T20:44:41.639737
        System.out.println("Saved updatedAt: " + saved.getUpdatedAt()); //Saved updatedAt: 2025-12-21T20:44:41.639737

        // Если даты изменились - проблема в аннотациях Hibernate
    }

}