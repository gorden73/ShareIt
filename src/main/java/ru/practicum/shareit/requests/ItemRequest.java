package ru.practicum.shareit.requests;

import lombok.Data;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private long id;
    @NotNull
    private String description;
    @NotNull
    private User requestor;
    private LocalDateTime created;
}
