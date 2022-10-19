package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ElementNotFoundException;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;

import java.util.Collection;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

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
        return userRepository.findById(id).orElseThrow(() -> new ElementNotFoundException(
                String.format("пользователь с id%d.", id)));
    }

    @Override
    public User updateUser(long userId, User updatedUser) {
        User user = getUserById(userId);
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
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
    public HttpStatus removeUserById(long id) {
        try {
            userRepository.deleteById(id);
            log.info(String.format("Удалён пользователь с id%d.", id));
            return HttpStatus.OK;
        } catch (Exception e) {
            log.error(String.format("Не найден пользователь с id%d.", id));
            throw new ElementNotFoundException(String.format("пользователь с id%d.", id));
        }
    }
}
