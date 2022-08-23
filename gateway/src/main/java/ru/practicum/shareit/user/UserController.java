package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public Object getAllUsers() {
        return userClient.getAllUsers();
    }

    @PostMapping
    public Object addUser(@RequestBody UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("user.Email = null или состоит из пробелов.");
        }
        checkValidEmailAddress(userDto.getEmail());
        return userClient.addUser(userDto);
    }

    @GetMapping("/{userId}")
    public Object getUserById(@PathVariable long userId) {
        return userClient.getUserById(userId);
    }

    @PatchMapping("/{userId}")
    public Object updateUser(@PathVariable long userId,
                             @RequestBody UserDto userDto) {
        if (userDto.getEmail() != null) {
            checkValidEmailAddress(userDto.getEmail());
        }
        return userClient.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public Object removeUserById(@PathVariable long userId) {
        return userClient.removeUserById(userId);
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
