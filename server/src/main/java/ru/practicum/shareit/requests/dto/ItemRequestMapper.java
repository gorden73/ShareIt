package ru.practicum.shareit.requests.dto;

import ru.practicum.shareit.requests.ItemRequest;

import java.util.Collection;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                request.getItems());
    }

    public static ItemRequest toRequest(ItemRequestDto dto) {
        return new ItemRequest(dto.getDescription());
    }

    public static Collection<ItemRequestDto> toDtoCollection(Collection<ItemRequest> requestCollection) {
        return requestCollection
                .stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }
}