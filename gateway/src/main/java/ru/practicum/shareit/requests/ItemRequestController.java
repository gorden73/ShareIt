package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @PostMapping
    public Object addRequest(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                             @Valid @RequestBody ItemRequestDto dto) {
        return requestClient.addRequest(userId, dto);
    }

    @GetMapping
    public Object getAllItemRequestsByOwner(@RequestHeader("X-Sharer-User-Id") @NotNull long userId) {
        return requestClient.getAllItemRequestsByOwner(userId);
    }

    @GetMapping("/all")
    public Object getAllItemRequestsByOtherUsers(@RequestHeader("X-Sharer-User-Id")
                                                 @NotNull long userId,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size) {
        if (from < 0) {
            throw new ValidationException(String.format("неверное значение from %d.", from));
        }
        if (size < 1) {
            throw new ValidationException(String.format("неверное значение size %d.", size));
        }
        return requestClient.getAllItemRequestsByOtherUsers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public Object getItemRequestById(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                                     @PathVariable long requestId) {
        return requestClient.getItemRequestById(userId, requestId);
    }
}
