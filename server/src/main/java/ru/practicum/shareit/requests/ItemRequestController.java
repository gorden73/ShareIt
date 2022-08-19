package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestMapper;

import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestBody ItemRequestDto dto) {
        ItemRequest request = ItemRequestMapper.toRequest(dto);
        return ItemRequestMapper.toDto(service.addRequest(userId, request));
    }

    @GetMapping
    public Collection<ItemRequestDto> getAllItemRequestsByOwner(@RequestHeader("X-Sharer-User-Id")
                                                                long userId) {
        return ItemRequestMapper.toDtoCollection(service.getAllItemRequestsByOwner(userId));
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAllItemRequestsByOtherUsers(@RequestHeader("X-Sharer-User-Id")
                                                                     long userId,
                                                                     @RequestParam int from,
                                                                     @RequestParam int size) {
        return ItemRequestMapper.toDtoCollection(service.getAllItemRequestsByOtherUsers(userId, from, size));
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long requestId) {
        return ItemRequestMapper.toDto(service.getItemRequestById(userId, requestId));
    }
}
