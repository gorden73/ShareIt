package ru.practicum.shareit.item;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    Item addItem(long userId, Item item);

    Item updateItem(long userId, long itemId, Item updatedItem);

    Item getItemById(long userId, long id);

    Collection<Item> getOwnerItems(long ownerId, int from, int size);

    List<Item> searchAvailableItemsByRequestId(long requestId);

    Collection<Item> searchAvailableItems(String text, int from, int size);

    Comment addCommentByItemId(long userId, Comment comment, long itemId);

    void checkUserById(long userId);
}
