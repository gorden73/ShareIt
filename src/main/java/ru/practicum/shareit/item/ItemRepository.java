package ru.practicum.shareit.item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {
    Item addItem(Item item);

    Optional<Item> getItemById(long id);

    Item updateItem(Item item);

    Collection<Item> getUserItems(long userId);

    Collection<Item> searchAvailableItems(String text);
}
