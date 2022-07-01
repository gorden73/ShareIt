package ru.practicum.shareit.user;

import java.util.Collection;


public interface UserRepository {
    Collection<User> getAllUsers();

    User addUser(User user);

    User getUserById(long id);

    User updateUser(User user);

    long removeUserById(long id);
}
