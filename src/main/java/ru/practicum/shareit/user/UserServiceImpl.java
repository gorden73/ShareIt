package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.EmailAlreadyExistsException;
import ru.practicum.shareit.exceptions.ValidationException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Collection;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Collection<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public User addUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException(String.format("user.Email = null или состоит из пробелов."));
        }
        checkEmailAvailability(user.getEmail());
        return userRepository.addUser(user);
    }

    @Override
    public User getUserById(long id) {
        Optional<User> optionalUser = userRepository.getUserById(id);
        if (optionalUser.isEmpty()) {
            throw new ElementNotFoundException(String.format("Не найден пользователь с id%d.", id));
        }
        return optionalUser.get();
    }

    @Override
    public User updateUser(long userId, User updatedUser) {
        User user = getUserById(userId);
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            checkEmailAvailability(updatedUser.getEmail());
            user.setEmail(updatedUser.getEmail());
        }
        return userRepository.updateUser(user);
    }

    @Override
    public long removeUserById(long id) {
        return userRepository.removeUserById(id);
    }

    private void checkValidEmailAddress(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            throw new ValidationException(String.format("user.Email не в формате email."));
        }
    }

    private boolean checkEmailAvailability(String email) {
        checkValidEmailAddress(email);
        for (User u : getAllUsers()) {
            if (email.equals(u.getEmail())) {
                throw new EmailAlreadyExistsException(String.format("пользователь с таким email %s уже существует.",
                        email));
            }
        }
        return true;
    }
}
