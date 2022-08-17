package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()
        );
    }

    public Object addUser(UserDto userDto) {
        return post("", userDto);
    }

    public Object getAllUsers() {
        return get("");
    }

    public Object getUserById(long id) {
        return get("/" + id);
    }

    public Object updateUser(long userId, UserDto updatedUser) {
        return patch("/" + userId, updatedUser);
    }

    public void removeUserById(long id) {
        delete("/" + id);
    }
}
