package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotNull;
import java.util.List;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public Object addItem(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                          @RequestBody ItemDto itemDto) {
        checkInputDataByAddItem(itemDto);
        return itemClient.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public Object updateItem(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                             @RequestBody ItemDto itemDto,
                             @PathVariable long itemId) {
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public Object getItemById(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                              @PathVariable long itemId) {
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public Object getOwnerItems(@RequestHeader("X-Sharer-User-Id") @NotNull long ownerId,
                                @RequestParam(defaultValue = "0") int from,
                                @RequestParam(defaultValue = "10") int size) {
        checkPageBorders(from, size);
        return itemClient.getOwnerItems(ownerId, from, size);
    }

    @GetMapping("/search")
    public Object searchAvailableItems(@RequestParam String text,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        if (text.isBlank()) {
            log.info("Пустой поисковый запрос.");
            return List.of();
        }
        checkPageBorders(from, size);
        return itemClient.searchAvailableItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public Object addCommentByItemId(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                                     @RequestBody CommentDto commentDto,
                                     @PathVariable long itemId) {
        if (commentDto.getText().isBlank()) {
            log.error("Отзыв пустой или состоит из пробелов.");
            throw new ValidationException("отзыв пустой или состоит из пробелов.");
        }
        return itemClient.addCommentByItemId(userId, commentDto, itemId);
    }

    private void checkInputDataByAddItem(ItemDto item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("item.Name = null или item.Name состоит из пробелов.");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("item.Description = null или состоит из пробелов.");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("item.isAvailable = null.");
        }
    }

    private void checkPageBorders(int from, int size) {
        if (from < 0) {
            throw new ValidationException(String.format("недопустимое значение from %d.", from));
        }
        if (size < 1) {
            throw new ValidationException(String.format("недопустимое значение size %d.", size));
        }
    }
}
