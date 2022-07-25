package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public UserDto addUser(@RequestBody UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userService.addUser(user));
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable long userId) {
        return UserMapper.toUserDto(userService.getUserById(userId));
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable long userId,
                              @RequestBody UserDto userDto) {
        User updatedUser = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userService.updateUser(userId, updatedUser));
    }

    @DeleteMapping("/{userId}")
    public void removeUserById(@PathVariable long userId) {
        userService.removeUserById(userId);
    }
}
