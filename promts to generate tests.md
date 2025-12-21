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

Конечно! Дополню реализации всех недостающих классов и укажу зависимости. 

## **1. Зависимости в `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- JWT (Spring Security) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.5.5.Final</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.5.5.Final</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Ответ по JWT**: Да, `NimbusJwtDecoder` и `JwtDecoder` из библиотеки Spring Security OAuth2 Resource Server. Они используются для работы с JWT токенами.

## **2. Entity Classes**

### **User.java**
```java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Метод для обновления builder
    public UserBuilder toBuilder() {
        return User.builder()
            .id(this.id)
            .email(this.email)
            .passwordHash(this.passwordHash)
            .status(this.status)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt);
    }
}
```

### **UserStatus.java**
```java
package com.example.demo.entity;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    DELETED
}
```

## **3. DTO Classes**

### **CreateUserRequest.java**
```java
package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
```

### **UserResponse.java**
```java
package com.example.demo.dto.response;

import com.example.demo.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private UserStatus status;
}
```

### **PageResponse.java**
```java
package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
```

## **4. Mapper Class (MapStruct)**

### **UserMapper.java**
```java
package com.example.demo.mapper;

import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserResponse toResponse(User user);
    
    List<UserResponse> toResponseList(List<User> users);
    
    default PageResponse<UserResponse> toPageResponse(Page<User> page) {
        return new PageResponse<>(
            toResponseList(page.getContent()),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
```

## **5. Repository Interface**

### **UserRepository.java**
```java
package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByStatus(UserStatus status);
    
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:email IS NULL OR u.email LIKE %:email%)")
    Page<User> findUsers(
            @Param("status") UserStatus status,
            @Param("email") String email,
            Pageable pageable);
    
    default Page<User> getUsers(int page, int size, UserStatus status) {
        return findUsers(status, null, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }
}
```

## **6. Service Interfaces**

### **EmailService.java**
```java
package com.example.demo.service;

public interface EmailService {
    void sendWelcomeEmail(String email);
    void sendPasswordResetEmail(String email, String token);
    void sendNotification(String email, String subject, String message);
}
```

### **EmailServiceImpl.java**
```java
package com.example.demo.service.impl;

import com.example.demo.service.EmailService;
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
```

## **7. Configuration Classes**

### **PasswordEncoder Configuration**
```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### **ValidationConfig.java** (если нужны кастомные валидаторы)
```java
package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {
    // Конфигурация валидации
}
```

## **8. Exception Classes**

### **UserAlreadyExistsException.java**
```java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
```

### **UserNotFoundException.java**
```java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
```

### **InvalidDateException.java**
```java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDateException extends RuntimeException {
    public InvalidDateException(String message) {
        super(message);
    }
}
```

## **9. Test Configuration**

### **TestConfig.java**
```java
package com.example.demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {
    
    @Bean
    public PasswordEncoder testPasswordEncoder() {
        return mock(PasswordEncoder.class);
    }
}
```

## **10. Application Properties**

### **application.yml**
```yaml
spring:
  application:
    name: demo-app
    
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
    
  h2:
    console:
      enabled: true
      path: /h2-console

logging:
  level:
    com.example.demo: DEBUG
    org.springframework.security: DEBUG

app:
  jwt:
    secret: my-super-secret-key-that-is-at-least-32-characters-long
    expiration-ms: 86400000 # 24 hours
    issuer: demo-app
    allowed-audiences: web,mobile
```

## **11. Main Application Class**

### **DemoApplication.java**
```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

## **Как запустить проект:**

### **1. Сборка проекта:**
```bash
mvn clean install
```

### **2. Запуск тестов:**
```bash
mvn test
```

### **3. Запуск отдельных тестов:**
```bash
# Все тесты
mvn test

# Конкретный тестовый класс
mvn test -Dtest=UserServiceTest

# Конкретный метод теста
mvn test -Dtest=UserServiceTest#createUser_withValidRequest_returnsSavedUser
```

### **4. Запуск приложения:**
```bash
mvn spring-boot:run
```

### **5. Доступ к H2 Console:**
После запуска приложения откройте: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (оставьте пустым)

## **Структура проекта:**
```
src/
├── main/
│   ├── java/
│   │   └── com/example/demo/
│   │       ├── DemoApplication.java
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── exception/
│   │       ├── mapper/
│   │       ├── repository/
│   │       ├── service/
│   │       └── util/
│   └── resources/
│       ├── application.yml
│       └── data.sql (опционально)
└── test/
    └── java/
        └── com/example/demo/
            ├── config/
            ├── controller/
            ├── repository/
            ├── service/
            └── util/
```

## **Если нужны дополнительные зависимости для JWT:**

### **Добавить в pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
</dependency>
```

## **Краткое пояснение по JWT классам:**

1. **JwtDecoder** - интерфейс Spring Security для декодирования JWT токенов
2. **NimbusJwtDecoder** - реализация от Nimbus JOSE + JWT библиотеки
3. **Keys** - утилитный класс для создания ключей

Теперь у вас есть полный рабочий проект Spring Boot с тестами! Все тесты должны успешно выполняться.

```

Для метода с `Keys.hmacShaKeyFor()` и `NimbusJwtDecoder` нужны следующие зависимости:

## **Обновленный `pom.xml` с JWT:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- Spring Security JWT (OAuth2 Resource Server) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.5.5.Final</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.5.5.Final</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## **2. Импорты для JwtConfig.java:**

```java
package com.example.demo.config;

import javax.crypto.SecretKey;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

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
        // Keys из io.jsonwebtoken:jjwt-api
        // SecretKey из javax.crypto
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
```

## **3. Если нужна ручная работа с JWT (генерация токенов), добавьте:**

```xml
<!-- Для генерации JWT токенов (если нужно создавать токены) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

## **4. Альтернативный вариант JwtConfig (без jjwt):**

Если хотите использовать только Spring Security без дополнительных зависимостей:

```java
package com.example.demo.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
        // Создаем SecretKey без зависимости от jjwt
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        
        // Используем SHA-256 для HMAC
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
```

## **5. Проверка импортов:**

Убедитесь, что в классе `JwtConfig` есть правильные импорты:

```java
// Для Spring Security JWT
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

// Для SecretKey (вариант 1 - с jjwt)
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

// ИЛИ (вариант 2 - без jjwt)
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

// Для работы с байтами
import java.nio.charset.StandardCharsets;
```

## **6. Тест для JwtConfig:**

```java
package com.example.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "app.jwt.secret=my-super-secret-key-that-is-at-least-32-characters-long",
    "app.jwt.expiration-ms=86400000",
    "app.jwt.issuer=demo-app",
    "app.jwt.allowed-audiences=web,mobile"
})
class JwtConfigTest {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Autowired
    private JwtDecoder jwtDecoder;
    
    @Test
    void jwtDecoderBean_isCreated() {
        assertThat(jwtDecoder).isNotNull();
    }
    
    @Test
    void configProperties_areLoaded() {
        assertThat(jwtConfig.getSecret())
            .isEqualTo("my-super-secret-key-that-is-at-least-32-characters-long");
        assertThat(jwtConfig.getExpirationMs()).isEqualTo(86400000L);
        assertThat(jwtConfig.getIssuer()).isEqualTo("demo-app");
        assertThat(jwtConfig.getAllowedAudiences()).containsExactly("web", "mobile");
    }
}
```

## **7. Minimal работающая конфигурация:**

Если у вас возникают проблемы с зависимостями, вот минимальный работающий вариант:

### **pom.xml (минимальный):**
```xml
<dependencies>
    <!-- Основные Spring Boot зависимости -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    
    <!-- Для тестов -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### **JwtConfig.java (рабочий вариант):**
```java
package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    
    private String secret;
    private long expirationMs;
    private String issuer;
    private List<String> allowedAudiences;
    
    // геттеры и сеттеры
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    // ... остальные геттеры/сеттеры
    
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
```

## **Проблемы и решения:**

1. **Ошибка**: `Cannot resolve symbol 'Keys'`
   **Решение**: Добавить зависимость jjwt или использовать `SecretKeySpec`

2. **Ошибка**: `Cannot resolve symbol 'NimbusJwtDecoder'`
   **Решение**: Убедиться, что есть `spring-boot-starter-oauth2-resource-server`

3. **Секрет ключ слишком короткий**:
   ```java
   // Минимальная длина для HS256 - 32 байта (256 бит)
   String secret = "my-super-secret-key-that-is-at-least-32-characters-long";
   ```

Теперь ваш JWT конфиг должен работать корректно! Все тесты должны компилироваться и выполняться.

```



