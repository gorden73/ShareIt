package ru.practicum.shareit.item.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class ItemDto extends ItemOwnerDto {
    public ItemDto(long id, String name, String description, Boolean available) {
        super(id, name, description, available);
    }
}
