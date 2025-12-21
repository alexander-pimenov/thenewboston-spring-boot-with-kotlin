package tv.codealong.tutorials.springboot.thenewboston.second_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

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
                .email(request.getEmail())
                .passwordHash("encodedPassword")
                .status(UserStatus.ACTIVE)
                .build();
        User savedUser = userToSave.toBuilder().id(1L).build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting(User::getId, User::getEmail, User::getStatus)
                .containsExactly(1L, "test@example.com", UserStatus.ACTIVE);

        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(request.getEmail()) &&
                        user.getPasswordHash().equals("encodedPassword")
        ));
        verify(emailService).sendWelcomeEmail(request.getEmail());
    }


    @Test
    @DisplayName("createUser_withExistingEmail_throwsException")
    void createUser_withExistingEmail_throwsException() {
        // Arrange
        String mail = "existing@example.com";
        CreateUserRequest request = new CreateUserRequest(mail, "password");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage(String.format("User with email: %s already exists", mail));

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
                .hasMessage(String.format("User with id=%s not found", invalidId));
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
            assertThat(result.size()).isEqualTo(2);
            //assertThat(result).hasSize(2).allMatch(user -> user.getStatus() == UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("getActiveUsers_whenNoActiveUsers_returnsEmptyList")
        void getActiveUsers_whenNoActiveUsers_returnsEmptyList() {
            // Arrange
            when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(Collections.emptyList());

            // Act
            List<User> result = userService.getActiveUsers();

            // Assert
            assertThat(result.size()).isEqualTo(0);
        }
    }
}