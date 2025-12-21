package tv.codealong.tutorials.springboot.thenewboston.second_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import tv.codealong.tutorials.various.yandex.task4_1_AuthenticationService.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    public PasswordEncoder testPasswordEncoder() {
        return mock(PasswordEncoder.class);
    }

    @Bean
    public UserMapper userMapper() {
        return new UserMapper() {
            @Override
            public UserResponse toResponse(User user) {
                return user != null ?
                        new UserResponse(user.getId(), user.getEmail(), user.getStatus()) :
                        null;
            }

            @Override
            public List<UserResponse> toResponseList(List<User> users) {
                return users != null ?
                        users.stream().map(this::toResponse).collect(Collectors.toList()) :
                        List.of();
            }
        };
    }
}
