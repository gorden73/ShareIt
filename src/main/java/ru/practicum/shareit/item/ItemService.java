package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collection;

public interface ItemService {
    Item addItem(long userId, Item item);

    Item updateItem(long userId, long itemId, Item updatedItem);

    Item getItemById(long id);

    Collection<Item> getUserItems(long userId);

    Collection<Item> searchAvailableItems(String text);

    Comment addCommentByItemId(Comment comment, long itemId);
}
