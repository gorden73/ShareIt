package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDto extends ItemOwnerDto {
    public ItemDto(long id, String name, String description, Boolean available) {
        super(id, name, description, available);
    }
}
