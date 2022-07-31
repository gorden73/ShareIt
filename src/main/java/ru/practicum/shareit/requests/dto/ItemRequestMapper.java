package ru.practicum.shareit.requests.dto;

import ru.practicum.shareit.requests.ItemRequest;

import java.util.Collection;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest item) {
        return new ItemRequestDto(
                item.getId(),
                item.getDescription(),
                item.getRequester().getId(),
                item.getCreated(),
                item.getItems());
    }

    public static Collection<ItemRequestDto> toDtoCollection(Collection<ItemRequest> requestCollection) {
        return requestCollection
                .stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }
}
