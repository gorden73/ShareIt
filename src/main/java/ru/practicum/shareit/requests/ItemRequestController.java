package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestMapper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                                     @Valid @RequestBody ItemRequestDto dto) {
        ItemRequest request = new ItemRequest(dto.getDescription());
        return ItemRequestMapper.toDto(service.addRequest(userId, request));
    }

    @GetMapping
    public Collection<ItemRequestDto> getAllItemRequestsByOwner(
            @RequestHeader("X-Sharer-User-Id") @NotNull long userId) {
        return ItemRequestMapper.toDtoCollection(service.getAllItemRequestsByOwner(userId));
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAllItemRequestsByOtherUsers(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                                                                     @RequestParam(defaultValue = "0") int from,
                                                                     @RequestParam(defaultValue = "10") int page) {
        return ItemRequestMapper.toDtoCollection(service.getAllItemRequestsByOtherUsers(userId, from, page));
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                                             @PathVariable long requestId) {
        return ItemRequestMapper.toDto(service.getItemRequestById(userId, requestId));
    }
}
