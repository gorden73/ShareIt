package ru.practicum.shareit.requests.dto;

import ru.practicum.shareit.requests.ItemRequest;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest item) {
        return new ItemRequestDto(
                item.getDescription(),
                item.getRequester().getId(),
                item.getCreated());
    }
}
