package tv.codealong.tutorials.springboot.thenewboston.second_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ExtendWith(SpringExtension.class)
//@TestPropertySource(properties = "spring.security.enabled=false")
@WebMvcTest(UserController.class)
@Import({UserMapper.class, ValidationConfig.class, TestSecurityConfig.class, GlobalExceptionHandler.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // ← ГЛАВНОЕ
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PasswordEncoder passwordEncoder; // Если используется


    @BeforeEach
    void setUp() {
        // Не нужен @BeforeEach для reset() - контекст пересоздается!
        // Сбрасываем все моки перед каждым тестом
        //Mockito.reset(userService);

        // Настраиваем общие моки, если нужно
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Настраиваем дефолтное поведение для избежания NPE
//        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
//        when(userService.getUsers(anyInt(), anyInt(), any())).thenReturn(emptyPage);
    }

    @Test
    @DisplayName("POST /api/v1/users - создание пользователя")
    void createUser_withValidRequest_returnsCreated() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("test@example.com", "Password123!");
        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .status(UserStatus.ACTIVE)
                .build();
        UserResponse expectedResponse = new UserResponse(1L, "test@example.com", UserStatus.ACTIVE);

        // Явно указываем, что метод должен быть вызван с любым аргументом
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(user);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Проверяем, что метод был вызван
        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
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

        // Проверяем, что сервис НЕ был вызван
        verify(userService, never()).createUser(any(CreateUserRequest.class));
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

        // Явно настраиваем мок для КОНКРЕТНОГО ID
        when(userService.getUserById(userId)).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        verify(userService, times(1)).getUserById(userId);
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

        verify(userService, times(1)).getUserById(userId);
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

        verify(userService, times(1)).getUsers(0, 10, null);
    }

    @Test
    @DisplayName("GET /api/v1/users - без параметров пагинации")
    void getUsers_withoutPagination_returnsDefaultPage() throws Exception {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(
                User.builder().id(1L).email("user@example.com").build()
        ));

        // Настраиваем для вызова с дефолтными параметрами
        when(userService.getUsers(0, 10, null)).thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUsers(0, 10, null);
    }

}