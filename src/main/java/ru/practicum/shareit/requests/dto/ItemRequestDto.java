package ru.practicum.shareit.requests.dto;

import lombok.Data;
import ru.practicum.shareit.requests.ItemRequest;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ItemRequestDto {
    private long id;
    @NotNull
    private String description;
    @NotNull
    private long requestor;
    private LocalDateTime created;

    public ItemRequestDto(String description, long requestor, LocalDateTime created) {
        this.description = description;
        this.requestor = requestor;
        this.created = created;
    }
    public static ItemRequestDto toItemRequestDto(ItemRequest item) {
        return new ItemRequestDto(
                item.getDescription(),
                item.getRequestor().getId(),
                item.getCreated());
    }
}
