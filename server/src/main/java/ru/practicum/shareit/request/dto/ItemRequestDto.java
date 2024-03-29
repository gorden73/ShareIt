package ru.practicum.shareit.request.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemOwnerDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ItemRequestDto {
    private long id;
    private String description;
    private LocalDateTime created;
    private List<ItemOwnerDto> items;

    public ItemRequestDto(long id, String description, LocalDateTime created, List<ItemOwnerDto> items) {
        this.id = id;
        this.description = description;
        this.created = created;
        this.items = items;
    }
}
