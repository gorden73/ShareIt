package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemDto extends ItemOwnerDto {
    public ItemDto(long id, String name, String description, Boolean available, long requestId) {
        super(id, name, description, available, requestId);
    }
}
