package ru.practicum.shareit.user;

import org.springframework.http.HttpStatus;

import java.util.Collection;

public interface UserService {
    Collection<User> getAllUsers();

    User addUser(User user);

    User getUserById(long id);

    User updateUser(long userId, User updatedUser);

    HttpStatus removeUserById(long id);
}
