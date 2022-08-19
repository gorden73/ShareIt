package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemOwnerDto;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @RequestBody ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemService.addItem(userId, item));
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable long itemId) {
        Item updatedItem = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemService.updateItem(userId, itemId, updatedItem));
    }

    @GetMapping("/{itemId}")
    public ItemOwnerDto getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long itemId) {
        Item item = itemService.getItemById(userId, itemId);
        if (item.getOwner().getId() == userId) {
            return ItemMapper.toItemOwnerDto(item);
        }
        return ItemMapper.toItemDto(item);
    }

    @GetMapping
    public Collection<ItemOwnerDto> getOwnerItems(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                  @RequestParam int from,
                                                  @RequestParam int size) {
        return itemService.getOwnerItems(ownerId, from, size)
                .stream()
                .map(ItemMapper::toItemOwnerDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchAvailableItems(@RequestParam String text,
                                                    @RequestParam int from,
                                                    @RequestParam int size) {
        return itemService.searchAvailableItems(text, from, size)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addCommentByItemId(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestBody CommentDto commentDto,
                                         @PathVariable long itemId) {
        Comment comment = ItemMapper.toComment(commentDto);
        return ItemMapper.toCommentDto(itemService.addCommentByItemId(userId, comment, itemId));
    }
}
