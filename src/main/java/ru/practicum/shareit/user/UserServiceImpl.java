package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.EmailAlreadyExistsException;
import ru.practicum.shareit.exceptions.ValidationException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Collection<User> getAllUsers() {
        log.info("Запрошен список всех пользователей.");
        return userRepository.findAll();
    }

    @Override
    public User addUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("user.Email = null или состоит из пробелов.");
        }
        checkValidEmailAddress(user.getEmail());
        try {
            log.info("Добавлен пользователь {}.", user);
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.error("Пользователь с таким email {} уже существует.", user.getEmail());
            throw new EmailAlreadyExistsException(String.format("Пользователь с таким email %s уже существует.",
                    user.getEmail()));
        }
    }

    @Override
    public User getUserById(long id) {
        log.info(String.format("Запрошен пользователь с id%d.", id));
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new ElementNotFoundException(String.format("пользователь с id%d.", id));
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
            checkValidEmailAddress(updatedUser.getEmail());
            user.setEmail(updatedUser.getEmail());
        }
        try {
            log.info(String.format("Обновлён пользователь с id%d.", userId));
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.error("Пользователь с таким email {} уже существует.", user.getEmail());
            throw new EmailAlreadyExistsException(String.format("Пользователь с таким email %s уже существует.",
                    user.getEmail()));
        }
    }

    @Override
    public void removeUserById(long id) {
        log.info(String.format("Удалён пользователь с id%d.", id));
        userRepository.deleteById(id);
    }

    private void checkValidEmailAddress(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            throw new ValidationException("user.Email не в формате email.");
        }
    }
}
