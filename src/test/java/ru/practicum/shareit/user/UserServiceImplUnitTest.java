package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.EmailAlreadyExistsException;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplUnitTest {

    private final UserService userService;

    @MockBean
    private UserRepository userRepository;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User("John1", "doe1@mail.ru");
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(user1);
        when(userRepository.findAll())
                .thenReturn(users);
        Collection<User> users1 = userService.getAllUsers();
        assertThat(users1.size(), equalTo(users.size()));
        assertThat(users1.stream().findFirst().get(), equalTo(user1));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddUserWhenUserNotExistsAndEmailIsNull() {
        user1.setEmail(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> userService.addUser(user1));
        assertTrue(exception.getMessage().contains("user.Email = null или состоит из пробелов."));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddUserWhenUserNotExistsAndEmailIsBlank() {
        user1.setEmail("");
        ValidationException exception = assertThrows(ValidationException.class, () -> userService.addUser(user1));
        assertTrue(exception.getMessage().contains("user.Email = null или состоит из пробелов."));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddUserWhenUserNotExistsAndEmailIsNotValid() {
        user1.setEmail("show.me.now.");
        ValidationException exception = assertThrows(ValidationException.class, () -> userService.addUser(user1));
        assertTrue(exception.getMessage().contains("user.Email не в формате email."));
    }

    @Test
    void shouldThrowEmailAlreadyExistsExceptionWhenAddUserWhenUserIsExists() {
        when(userRepository.save(any(User.class)))
                .thenThrow(DataIntegrityViolationException.class);
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.addUser(user1));
        assertTrue(exception.getMessage().contains("Пользователь с таким email"));
    }

    @Test
    void shouldReturnUserById() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        User userById = userService.getUserById(1L);
        assertThat(userById.getId(), equalTo(user1.getId()));
        assertThat(userById.getName(), equalTo(user1.getName()));
        assertThat(userById.getEmail(), equalTo(user1.getEmail()));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenGetNotExistsUserById() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> userService.getUserById(1L));
        assertTrue(exception.getMessage().contains("пользователь с id"));
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateUserWhenEmailIsNotValid() {
        user2 = new User(2L, "John2", "show.me.now.");
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        ValidationException exception = assertThrows(ValidationException.class, () -> userService.updateUser(1L,
                user2));
        assertTrue(exception.getMessage().contains("user.Email не в формате email."));
    }

    @Test
    void shouldThrowEmailAlreadyExistsExceptionWhenUpdateUserWhenUserIsExists() {
        user2 = new User(2L, "John2", "show@mail.ru");
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        when(userRepository.save(any(User.class)))
                .thenThrow(DataIntegrityViolationException.class);
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.updateUser(1L, user2));
        assertTrue(exception.getMessage().contains("Пользователь с таким email"));
    }

    @Test
    void shouldUpdateUser() {
        user2 = new User(2L, "John2", "show@mail.ru");
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        when(userRepository.save(any(User.class)))
                .thenReturn(user2);
        user2.setId(1L);
        User updatedUser = userService.updateUser(1L, user2);
        assertThat(updatedUser.getId(), equalTo(1L));
        assertThat(updatedUser.getName(), equalTo(user2.getName()));
        assertThat(updatedUser.getEmail(), equalTo(user2.getEmail()));
    }

    @Test
    void shouldRemoveUserById() {
        userService.removeUserById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}