package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(locations = "classpath:application-test.properties")
class UserServiceImplIntegrationTest {

    private final UserService userService;
    private User user1;

    @BeforeEach
    void setUp() {
        user1 = new User("John", "doe@mail.ru");
    }

    @Test
    void shouldAddUserWhenUserValidAndNotExists() {
        User user2 = userService.addUser(user1);
        assertThat(user2.getId(), notNullValue());
        assertThat(user2.getName(), equalTo(user1.getName()));
        assertThat(user2.getEmail(), equalTo(user1.getEmail()));
    }
}