Привет! Ты - крутой программист Java/Koltin! Хочу стпросить тебя , как специалиста в области AI-агентов. Мне рассказали, как можно к моей среде разоаботки GIGA IDEA PRO 2025.1 подключить в плагинах агента ,кторый может создавать тесты на написанный функционал в нашем spring boot +  java приложении. И в настрйках плагина 'Auto tests' я нашел промты. Посмотри на них и скажи своё мнение, как они составлены, достаточно ли этого для хорошего создания тестов?
```
Promts to generate test and to correct the test.

Promt to generate test:

Проанализируй приведённый ниже код класса и создай комплексный набор (comprehensive suite) позитивных тестов с использованием применимых эффективных фреймворков.
ТРЕБОВАНИЯ К ТЕСТАМ:
1. ПОЛНОЕ ПОКРЫТИЕ - ТЕСТАМИ ДОЛЖНЫ БЫТЬ ПОКРЫТЫ:
- Все публичные методы класса
- Все возможные сценарии использования
- Конструктор и инициализация зависимостей
2. ПРОВЕРКА ДАННЫХ - ТЕСТАМИ ДОЛЖНЫ БЫТЬ ПРОВЕРЕНЫ:
- Допустимые форматы дат (ISO, граничные значения)
- Диапазоны числовых параметров (положительные, нулевые, отрицательные)
- Корректность парсинга и преобразования типов
3. ГРАНИЧНЫЕ СИТУАЦИИ - ТЕСТАМИ ДОЛЖНЫ БЫТЬ ПРОВЕРЕНЫ:
- Минимальные/максимальные значения
- Нулевые значения где допустимо
- Високосные годы, концы месяцев
- Разные временные зоны (если применимо)
4. СЦЕНАРИИ ТЕСТИРОВАНИЯ - ТЕСТАМИ ДОЛЖНЫ БЫТЬ ПРОВЕРЕНЫ:
- Нормальное выполнение с типичными данными
- Крайние случаи входных параметров
- Разные комбинации конфигураций
- Поведение при "пограничных" датах
5. КАЧЕСТВО ТЕСТОВ - ТЕСТЫ ДОЛЖНЫ ОБЕСПЕЧИТЬ:
- Параметризованные тесты для множества сценариев
- Четкие именования тестов по шаблону "methodName_scenario_expectedResult"
- Изоляция тестов друг от друга
- Mocking внешних зависимостей
- Проверка всех выходных параметров и форматов
6. ОСОБЫЕ ТРЕБОВАНИЯ - ТЕСТЫ ДОЛЖНЫ ОБЕСПЕЧИТЬ:
- Для методов, зависящих от текущего времени, использовать MockedStatic
- Проверять не только значения, но и форматы строк
- Тестировать исключения там, где они ожидаются
- Включать тесты на производительность если критично
- Для класса, зависимого от конфигурации включи:
- Тесты с разными конфигурационными сценариями
- Проверки валидации конфигурационных параметров
- Поведение при отсутствующих/невалидных конфигурациях
7. ТЕСТЫ ДОЛЖНЫ ИСПОЛЬЗОВАТЬ ФРЕЙМВОРКИ (ЕСЛИ ДОПУСТИМО):
- JUnit 5 (включая @ParameterizedTest)
- TestNG
- Mockito (включая mockStatic)
- AssertJ для fluent assertions
- Selenium (или Selenide/ JDI) — для тестирования интерфейса (UI-тесты).
- RestAssured — для тестирования REST API.
- JmeterJavaDsl - для нагрузочных тестов.
ДАЙ ПОЯСНЕНИЯ К КЛЮЧЕВЫМ ТЕСТОВЫМ СЦЕНАРИЯМ.
7. ТРЕБОВАНИЕ К СБОРКЕ:
- Сборка проекта должна проходить без ошибок
КОД КЛАССА:


Promt to correct the test:

Проанализируй сгенерированный тобой комплексный набор позитивных тестов для вышеуказанного класса  на соответствие следующим требованиям.
ТРЕБОВАНИЯ К ТЕСТАМ:
1. ПОЛНОЕ ПОКРЫТИЕ СЦЕНАРИЕВ - ТЕСТАМИ ДОЛЖНЫ БЫТЬ ПОКРЫТЫ:
- Все публичные методы класса.
- Все приватные методы, если это целесообразно (например, через тестирование публичного API, вызывающего приватные методы).
- Все возможные сценарии нормального использования.
- Конструктор и инициализация зависимостей.
- Статические методы и метод main, если он присутствует.
- Все ветвления логики (условия if, switch, циклы) внутри методов при передаче корректных данных.
2. ПРОВЕРКА ДАННЫХ - ТЕСТАМИ ДОЛЖНЫ БЫТЬ ПРОВЕРЕНЫ:
- Допустимые форматы дат (ISO 8601, граничные значения).
- Диапазоны числовых параметров (положительные, нулевые, отрицательные, если логически допустимо).
- Корректность парсинга и преобразования типов при корректных входных данных.
- Поведение при валидных строках, числах, датах.
3. ГРАНИЧНЫЕ СИТУАЦИИ - ТЕСТАМИ ДОЛЖНЫ БЫТЬ ПРОВЕРЕНЫ:
- Минимальные/максимальные допустимые значения числовых параметров.
- Нулевые значения, где логически допустимо.
- Високосные годы, концы месяцев, концы лет.
- Разные временные зоны (если применимо и влияет на логику).
- Крайние допустимые значения дат и продолжительностей.
4. СЦЕНАРИИ ТЕСТИРОВАНИЯ - ТЕСТАМИ ДОЛЖНЫ БЫТЬ ПРОВЕРЕНЫ:
- Нормальное выполнение с типичными данными.
- Крайние допустимые случаи входных параметров.
- Разные комбинации валидных конфигураций (если применимо).
- Поведение при "пограничных", но корректных датах и значениях.
- Сценарии, изменяющие состояние объекта при корректных операциях.
5. КАЧЕСТВО ТЕСТОВ - ТЕСТЫ ДОЛЖНЫ ОБЕСПЕЧИВАТЬ:
- Параметризованные тесты (@ParameterizedTest) для проверки множества валидных сценариев с разными входными данными.
- Четкие именования тестов по шаблону methodName_scenario_expectedResult (например, global_withValidConfig_returnsCorrectTimestamp).
- Изоляция тестов друг от друга (использование @BeforeEach, @AfterEach, избегание общего состояния между тестами).
- Mocking внешних зависимостей (@Mock, Mockito.when, Mockito.verify) для изоляции тестируемого класса.
- Проверка всех выходных параметров, возвращаемых значений, форматов строк, изменений состояния при нормальном выполнении.
- Использование подходящих assertThat из AssertJ для читаемости и точности проверок.
6. ОСОБЫЕ ТРЕБОВАНИЯ - ТЕСТЫ ДОЛЖНЫ ОБЕСПЕЧИВАТЬ:
- Для методов, зависящих от текущего времени, использовать MockedStatic<LocalDateTime> или аналогичные для LocalDate, Instant и т.д.
- Проверять форматы строк (например, с помощью String.matches(regex) или assertThat(result).startsWith("now")).
- Включать тесты на производительность (например, assertThat(executionTime).isLessThan(threshold)), если критично или явно запрошено.
- Проверять логику метода main при нормальном выполнении.
- Проверять корректность инициализации при передаче валидных зависимостей в конструкторе. 
- Для класса, зависимого от конфигурации, включи:
- Тесты с разными валидными конфигурационными сценариями.
- Поведение при корректных значениях конфигурации.
- Проверки валидации конфигурационных параметров
7. ТЕСТЫ ДОЛЖНЫ ИСПОЛЬЗОВАТЬ ФРЕЙМВОРКИ (ЕСЛИ ДОПУСТИМО):
- JUnit 5 (включая @ParameterizedTest, @Nested, @DisplayName, @TestFactory).
- Mockito (включая mockStatic, @Mock, @Spy, ArgumentCaptor).
- AssertJ для fluent assertions.
- (Опционально) Hamcrest для сложных матчингов.
- (Опционально) JsonPath для проверки JSON (если применимо).
8. ДОПОЛНИТЕЛЬНЫЕ УКАЗАНИЯ:
- Тесты должны быть независимыми от внешних факторов (время, файловая система, сеть).
- Тесты должны быть стабильными (детерминированными) и не зависеть от порядка выполнения.
- Используйте try-with-resources для MockedStatic, чтобы гарантировать восстановление оригинального поведения.
- Для захвата System.out используйте ByteArrayOutputStream и PrintStream (если main печатает результат).
- Комментарии в тестах должны пояснять цель теста и ожидаемый результат при нормальном сценарии.
ЕСЛИ СГЕНЕРИРОВАННЫЕ ТОБОЙ ТЕСТЫ НЕ ВПОЛНЕ СООТВЕТСТВУЮТ УКАЗАННЫМ ТРЕБОВАНИЯМ, ТО ПРЕДСТАВЬ ПОЛНЫЙ УЛУЧШЕННЫЙ КОД КОМПЛЕКСНОГО НАБОРА ПОЗИТИВНЫХ ТЕСТОВ.
КОД ТЕСТА:

```

Отличные промты! Как профессиональный Java/Kotlin разработчик и специалист по AI-агентам, проанализирую их качество.

## **Сильные стороны промтов:**

### 1. **Исчерпывающая структура**
- Чёткая иерархия требований (7 основных категорий)
- Конкретные технические указания
- Упоминание современных фреймворков (JUnit 5, Mockito, AssertJ)

### 2. **Профессиональные требования**
- Упор на параметризованные тесты
- Требование изоляции тестов
- Учёт временных зависимостей (MockedStatic)
- Граничные случаи и edge cases

### 3. **Промпт для коррекции** - отличная идея!
- AI часто "забывает" детали при генерации
- Второй промпт позволяет исправить упущения
- Добавляет конкретику по приватным методам и ветвлениям

## **Проблемы и недостатки:**

### 1. **Избыточная сложность для AI**
```
❌ ПРОБЛЕМА: Слишком много требований сразу
```
- AI может "потерять" важные аспекты
- Некоторые требования противоречивы (UI-тесты для Spring Boot класса)

### 2. **Конфликтующие указания**
```yaml
# В первом промпте:
7. ТЕСТЫ ДОЛЖНЫ ИСПОЛЬЗОВАТЬ ФРЕЙМВОРКИ:
- Selenium — для тестирования интерфейса (UI-тесты)
- RestAssured — для тестирования REST API
- JmeterJavaDsl - для нагрузочных тестов

# Но промт предназначен для тестирования класса!
```
→ **UI/нагрузочные тесты не имеют смысла для unit-тестов класса**

### 3. **Отсутствие контекста проекта**
- Нет указаний на структуру проекта (Maven/Gradle)
- Не указаны версии фреймворков
- Нет требований к структуре тестов (пакеты, классы)

### 4. **Дублирование номеров разделов**
- В обоих промтах два раза "7. ТЕСТЫ ДОЛЖНЫ ИСПОЛЬЗОВАТЬ..."

## **Мои рекомендации по улучшению:**

### **Упрощённый и более эффективный промпт:**

```markdown
Проанализируй Java-код класса ниже и создай комплексные unit-тесты.

ТРЕБОВАНИЯ К ТЕСТАМ:

1. ТЕХНИЧЕСКИЙ СТЕК:
- Java 17+, Spring Boot 3.x
- JUnit 5 + Mockito 5.x + AssertJ
- Используй @ParameterizedTest для разных сценариев

2. ОСНОВНЫЕ СЦЕНАРИИ:
- Позитивные тесты всех публичных методов
- Граничные значения (null, пустые строки, min/max)
- Временные зависимости замокай через MockedStatic
- Проверь исключения в негативных сценариях

3. КАЧЕСТВО КОДА:
- Каждый тест независим
- Чёткие имена: methodName_scenario_expected
- Используй @Nested для группировки тестов
- Mock все внешние зависимости

4. ОСОБЫЕ УКАЗАНИЯ:
- Не генерируй интеграционные/UI тесты
- Фокус только на unit-тестировании класса
- Добавь комментарии о цели каждого теста

КОД КЛАССА ДЛЯ ТЕСТИРОВАНИЯ:
```

### **Промпт для коррекции (улучшенный):**

```markdown
Проверь сгенерированные тесты на соответствие:

1. КРИТИЧЕСКИЕ ПРОПУСКИ:
- Проверил ли ты все публичные методы?
- Есть ли тесты на исключения?
- Замоканы ли внешние зависимости?

2. КАЧЕСТВО ТЕСТОВ:
- Тесты изолированы?
- Имена понятны?
- Используются параметризованные тесты где уместно?

3. ГРАНИЧНЫЕ СЛУЧАИ:
- null значения
- пустые коллекции/строки
- минимальные/максимальные значения

Исправь любые найденные проблемы и представь улучшенную версию.
```

## **Практический совет для GIGA IDEA PRO 2025.1:**

### 1. **Используйте цепочку промптов:**
```
1. Базовый тест → 2. Добавить моки → 3. Добавить граничные случаи
```

### 2. **Добавьте контекст проекта в системный промпт:**
```yaml
Project context:
- Java 17
- Spring Boot 3.2.4
- JUnit 5.10.0
- Mockito 5.7.0
- Maven build
```

### 3. **Пример эффективного использования:**

**Первый запрос:**
```
Создай базовые unit-тесты для класса UserService с использованием JUnit 5 и Mockito
```

**Второй запрос (после получения результата):**
```
Добавь параметризованные тесты для метода validateEmail с разными форматами email
```

**Третий запрос:**
```
Добавь тесты на исключения и граничные случаи для всех методов
```

## **Вывод:**

Ваши промпты **хорошо структурированы**, но **слишком сложны для одноразового выполнения**. AI теряет важные детали в таком объёме требований.

**Рекомендую:**
1. Упростить основной промпт
2. Убрать противоречивые требования (UI/load тесты)
3. Использовать поэтапный подход
4. Добавить контекст проекта

Хотите, я помогу составить оптимальные промпты конкретно под вашу кодовую базу и требования? Могу также показать примеры эффективных тестов для типичных Spring Boot компонентов.

```
Отлично! Покажу вам примеры **эффективных тестов для типичных Spring Boot компонентов**. Эти примеры можно использовать как шаблоны и для обучения AI-агента.

## **1. Service Layer (Сервисный слой)**

### **UserService.java**
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }
        
        User user = User.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .status(UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .build();
            
        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmail(savedUser.getEmail());
        
        return savedUser;
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
    
    public List<User> getActiveUsers() {
        return userRepository.findByStatus(UserStatus.ACTIVE);
    }
}
```

### **UserServiceTest.java (Эффективный тест)**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("createUser_withValidRequest_returnsSavedUser")
    void createUser_withValidRequest_returnsSavedUser() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("test@example.com", "password123");
        User userToSave = User.builder()
            .email(request.email())
            .passwordHash("encodedPassword")
            .status(UserStatus.ACTIVE)
            .build();
        User savedUser = userToSave.toBuilder().id(1L).build();
        
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Act
        User result = userService.createUser(request);
        
        // Assert
        assertThat(result)
            .isNotNull()
            .extracting(User::getId, User::getEmail, User::getStatus)
            .containsExactly(1L, "test@example.com", UserStatus.ACTIVE);
        
        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(argThat(user -> 
            user.getEmail().equals(request.email()) &&
            user.getPasswordHash().equals("encodedPassword")
        ));
        verify(emailService).sendWelcomeEmail(request.email());
    }
    
    @Test
    @DisplayName("createUser_withExistingEmail_throwsException")
    void createUser_withExistingEmail_throwsException() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("existing@example.com", "password");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessage("User with email already exists");
        
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendWelcomeEmail(any());
    }
    
    @Test
    @DisplayName("getUserById_withExistingId_returnsUser")
    void getUserById_withExistingId_returnsUser() {
        // Arrange
        Long userId = 1L;
        User expectedUser = User.builder()
            .id(userId)
            .email("user@example.com")
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        
        // Act
        User result = userService.getUserById(userId);
        
        // Assert
        assertThat(result).isEqualTo(expectedUser);
        verify(userRepository).findById(userId);
    }
    
    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, 999L})
    @DisplayName("getUserById_withNonExistingId_throwsException")
    void getUserById_withNonExistingId_throwsException(Long invalidId) {
        // Arrange
        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(invalidId))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found");
    }
    
    @Nested
    @DisplayName("getActiveUsers Tests")
    class GetActiveUsersTests {
        
        @Test
        @DisplayName("getActiveUsers_whenUsersExist_returnsList")
        void getActiveUsers_whenUsersExist_returnsList() {
            // Arrange
            List<User> activeUsers = List.of(
                User.builder().id(1L).status(UserStatus.ACTIVE).build(),
                User.builder().id(2L).status(UserStatus.ACTIVE).build()
            );
            when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(activeUsers);
            
            // Act
            List<User> result = userService.getActiveUsers();
            
            // Assert
            assertThat(result)
                .hasSize(2)
                .allMatch(user -> user.getStatus() == UserStatus.ACTIVE);
        }
        
        @Test
        @DisplayName("getActiveUsers_whenNoActiveUsers_returnsEmptyList")
        void getActiveUsers_whenNoActiveUsers_returnsEmptyList() {
            // Arrange
            when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(Collections.emptyList());
            
            // Act
            List<User> result = userService.getActiveUsers();
            
            // Assert
            assertThat(result).isEmpty();
        }
    }
}
```

## **2. Controller Layer (REST Controller)**

### **UserController.java**
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return userMapper.toResponse(user);
    }
    
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return userMapper.toResponse(user);
    }
    
    @GetMapping
    public PageResponse<UserResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UserStatus status) {
        Page<User> users = userService.getUsers(page, size, status);
        return userMapper.toPageResponse(users);
    }
}
```

### **UserControllerTest.java (WebMvcTest)**
```java
@WebMvcTest(UserController.class)
@Import({UserMapperImpl.class, ValidationConfig.class})
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("POST /api/v1/users - создание пользователя")
    void createUser_withValidRequest_returnsCreated() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("test@example.com", "Password123!");
        User user = User.builder()
            .id(1L)
            .email(request.email())
            .status(UserStatus.ACTIVE)
            .build();
        UserResponse expectedResponse = new UserResponse(1L, "test@example.com", UserStatus.ACTIVE);
        
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(user);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    
    @ParameterizedTest
    @MethodSource("invalidCreateUserRequests")
    @DisplayName("POST /api/v1/users - валидация запроса")
    void createUser_withInvalidRequest_returnsBadRequest(
            CreateUserRequest invalidRequest,
            String expectedErrorField) throws Exception {
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].field").value(expectedErrorField));
    }
    
    private static Stream<Arguments> invalidCreateUserRequests() {
        return Stream.of(
            Arguments.of(new CreateUserRequest(null, "Password123!"), "email"),
            Arguments.of(new CreateUserRequest("invalid-email", "Password123!"), "email"),
            Arguments.of(new CreateUserRequest("test@example.com", null), "password"),
            Arguments.of(new CreateUserRequest("test@example.com", "short"), "password")
        );
    }
    
    @Test
    @DisplayName("GET /api/v1/users/{id} - получение пользователя")
    void getUser_withExistingId_returnsUser() throws Exception {
        // Arrange
        Long userId = 1L;
        User user = User.builder().id(userId).email("user@example.com").build();
        when(userService.getUserById(userId)).thenReturn(user);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.email").value("user@example.com"));
    }
    
    @Test
    @DisplayName("GET /api/v1/users/{id} - пользователь не найден")
    void getUser_withNonExistingId_returnsNotFound() throws Exception {
        // Arrange
        Long userId = 999L;
        when(userService.getUserById(userId))
            .thenThrow(new UserNotFoundException("User not found"));
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{id}", userId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("User not found"));
    }
    
    @Test
    @DisplayName("GET /api/v1/users - пагинация")
    void getUsers_withPagination_returnsPage() throws Exception {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(
            User.builder().id(1L).email("user1@example.com").build(),
            User.builder().id(2L).email("user2@example.com").build()
        ));
        
        when(userService.getUsers(0, 10, null)).thenReturn(userPage);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1));
    }
}
```

## **3. Repository Layer (Spring Data JPA)**

### **UserRepositoryTest.java (DataJpaTest)**
```java
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
        assertThat(foundUser)
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
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("findByStatus_withActiveUsers_returnsList")
    void findByStatus_withActiveUsers_returnsList() {
        // Arrange
        User activeUser = User.builder()
            .email("active@example.com")
            .status(UserStatus.ACTIVE)
            .build();
        User inactiveUser = User.builder()
            .email("inactive@example.com")
            .status(UserStatus.INACTIVE)
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
    
    @Test
    @DisplayName("findByCreatedAtBetween_returnsUsersInDateRange")
    void findByCreatedAtBetween_returnsUsersInDateRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        
        User user1 = User.builder()
            .email("user1@example.com")
            .createdAt(LocalDateTime.of(2024, 1, 15, 10, 0))
            .build();
            
        User user2 = User.builder()
            .email("user2@example.com")
            .createdAt(LocalDateTime.of(2024, 2, 1, 10, 0)) // Outside range
            .build();
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
        
        // Act
        List<User> users = userRepository.findByCreatedAtBetween(startDate, endDate);
        
        // Assert
        assertThat(users)
            .hasSize(1)
            .extracting(User::getEmail)
            .containsExactly("user1@example.com");
    }
}
```

## **4. Util/Helper Class (Утилитарный класс)**

### **DateUtils.java**
```java
@Component
public class DateUtils {
    
    public LocalDate parseIsoDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new InvalidDateException("Invalid ISO date format: " + dateString);
        }
    }
    
    public boolean isDateInFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }
    
    public LocalDate addBusinessDays(LocalDate date, int days) {
        LocalDate result = date;
        int addedDays = 0;
        
        while (addedDays < days) {
            result = result.plusDays(1);
            if (!isWeekend(result)) {
                addedDays++;
            }
        }
        return result;
    }
    
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    public String formatToIso(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }
}
```

### **DateUtilsTest.java (С использованием MockedStatic)**
```java
@ExtendWith(MockitoExtension.class)
class DateUtilsTest {
    
    private DateUtils dateUtils;
    
    @BeforeEach
    void setUp() {
        dateUtils = new DateUtils();
    }
    
    @Test
    @DisplayName("parseIsoDate_withValidFormat_returnsLocalDate")
    void parseIsoDate_withValidFormat_returnsLocalDate() {
        // Arrange
        String validDate = "2024-01-15";
        
        // Act
        LocalDate result = dateUtils.parseIsoDate(validDate);
        
        // Assert
        assertThat(result)
            .isEqualTo(LocalDate.of(2024, 1, 15));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"2024-13-01", "not-a-date", "2024/01/01"})
    @DisplayName("parseIsoDate_withInvalidFormat_throwsException")
    void parseIsoDate_withInvalidFormat_throwsException(String invalidDate) {
        // Act & Assert
        assertThatThrownBy(() -> dateUtils.parseIsoDate(invalidDate))
            .isInstanceOf(InvalidDateException.class)
            .hasMessageContaining("Invalid ISO date format");
    }
    
    @Test
    @DisplayName("isDateInFuture_whenDateIsFuture_returnsTrue")
    void isDateInFuture_whenDateIsFuture_returnsTrue() {
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            // Arrange
            LocalDate currentDate = LocalDate.of(2024, 1, 1);
            LocalDate futureDate = LocalDate.of(2024, 1, 2);
            
            mockedLocalDate.when(LocalDate::now).thenReturn(currentDate);
            
            // Act
            boolean result = dateUtils.isDateInFuture(futureDate);
            
            // Assert
            assertThat(result).isTrue();
        }
    }
    
    @Test
    @DisplayName("isDateInFuture_whenDateIsPast_returnsFalse")
    void isDateInFuture_whenDateIsPast_returnsFalse() {
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            // Arrange
            LocalDate currentDate = LocalDate.of(2024, 1, 15);
            LocalDate pastDate = LocalDate.of(2024, 1, 14);
            
            mockedLocalDate.when(LocalDate::now).thenReturn(currentDate);
            
            // Act
            boolean result = dateUtils.isDateInFuture(pastDate);
            
            // Assert
            assertThat(result).isFalse();
        }
    }
    
    @ParameterizedTest
    @CsvSource({
        "2024-01-15, 1, 2024-01-16",  // Понедельник +1 день
        "2024-01-19, 1, 2024-01-22",  // Пятница +1 день (пропускаем выходные)
        "2024-01-15, 5, 2024-01-22",  // 5 рабочих дней
        "2024-01-19, 3, 2024-01-24"   // Пятница +3 дня
    })
    @DisplayName("addBusinessDays_withDifferentInputs_returnsCorrectDate")
    void addBusinessDays_withDifferentInputs_returnsCorrectDate(
            String startDateStr,
            int daysToAdd,
            String expectedDateStr) {
        
        // Arrange
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate expectedDate = LocalDate.parse(expectedDateStr);
        
        // Act
        LocalDate result = dateUtils.addBusinessDays(startDate, daysToAdd);
        
        // Assert
        assertThat(result).isEqualTo(expectedDate);
    }
    
    @Test
    @DisplayName("formatToIso_withDate_returnsFormattedString")
    void formatToIso_withDate_returnsFormattedString() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        // Act
        String result = dateUtils.formatToIso(date);
        
        // Assert
        assertThat(result).isEqualTo("2024-01-15");
    }
}
```

## **5. Configuration Class (Класс конфигурации)**

### **JwtConfig.java**
```java
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
    
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
```

### **JwtConfigTest.java**
```java
@EnableConfigurationProperties(JwtConfig.class)
@ContextConfiguration(classes = JwtConfig.class)
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-1234567890",
    "app.jwt.expiration-ms=3600000",
    "app.jwt.issuer=my-app",
    "app.jwt.allowed-audiences=web,mobile"
})
class JwtConfigTest {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Test
    @DisplayName("loadConfig_withValidProperties_configurationLoaded")
    void loadConfig_withValidProperties_configurationLoaded() {
        // Assert
        assertThat(jwtConfig.getSecret()).isEqualTo("test-secret-key-1234567890");
        assertThat(jwtConfig.getExpirationMs()).isEqualTo(3600000);
        assertThat(jwtConfig.getIssuer()).isEqualTo("my-app");
        assertThat(jwtConfig.getAllowedAudiences())
            .containsExactly("web", "mobile");
    }
    
    @Test
    @DisplayName("jwtDecoderBean_returnsDecoder")
    void jwtDecoderBean_returnsDecoder() {
        // Act
        JwtDecoder jwtDecoder = jwtConfig.jwtDecoder();
        
        // Assert
        assertThat(jwtDecoder).isNotNull();
    }
}

// Тест на валидацию
@TestPropertySource(properties = {
    "app.jwt.secret=",  // пустой secret
    "app.jwt.expiration-ms=0",  // некорректное значение
    "app.jwt.issuer="
})
class JwtConfigValidationTest {
    
    @Test
    @DisplayName("loadConfig_withInvalidProperties_throwsException")
    void loadConfig_withInvalidProperties_throwsException() {
        assertThatThrownBy(() -> {
            new SpringApplicationBuilder(JwtConfig.class)
                .web(WebApplicationType.NONE)
                .run();
        }).hasRootCauseInstanceOf(ValidationException.class);
    }
}
```

## **Ключевые принципы эффективных тестов:**

### **1. Структура теста (AAA):**
```java
@Test
void methodName_scenario_expectedResult() {
    // Arrange - подготовка данных
    // Act - выполнение действия
    // Assert - проверка результата
}
```

### **2. Именование тестов:**
- `methodName_condition_result`
- `methodName_whenCondition_thenResult`
- Чётко отражает сценарий

### **3. Использование параметризации:**
```java
@ParameterizedTest
@ValueSource
@CsvSource
@MethodSource
```

### **4. Изоляция тестов:**
- Каждый тест независим
- @BeforeEach для общей подготовки
- Моки для внешних зависимостей

### **5. Проверка поведения:**
- Проверка возвращаемых значений
- Проверка исключений
- Проверка взаимодействия (verify)
- Проверка граничных случаев

## **Для AI-агента можно дать такие инструкции:**

```
При создании тестов следуй шаблону:
1. Используй JUnit 5 + Mockito + AssertJ
2. Каждый тест должен следовать паттерну AAA
3. Именуй тесты: methodName_scenario_expected
4. Покрой позитивные и негативные сценарии
5. Используй параметризованные тесты для множества данных
6. Замокай все внешние зависимости
7. Проверяй исключения через assertThatThrownBy
8. Для временных зависимостей используй MockedStatic
```

Эти примеры можно использовать как шаблоны для обучения AI или как эталон для проверки сгенерированных тестов.

```

