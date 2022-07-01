package ru.practicum.shareit.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;


@RestController
@RequestMapping(path = "/users")
public class UserController {
    private UserService userService;
    @GetMapping
    public Collection<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
