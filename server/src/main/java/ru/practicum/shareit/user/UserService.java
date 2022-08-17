package ru.practicum.shareit.user;

import java.util.Collection;

public interface UserService {
    Collection<User> getAllUsers();

    User addUser(User user);

    User getUserById(long id);

    User updateUser(long userId, User updatedUser);

    void removeUserById(long id);
}
