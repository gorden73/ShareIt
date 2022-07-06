package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    Collection<User> getAllUsers();

    User addUser(User user);

    Optional<User> getUserById(long id);

    User updateUser(User updatedUser);

    long removeUserById(long id);

    boolean checkUserById(long id);
}
