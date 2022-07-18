package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ItemRepositoryImpl {
    private final Map<Long, Item> items = new HashMap<>();
    private long id = 1;

    public Item addItem(Item item) {
        item.setId(id);
        items.put(id, item);
        log.info("Добавлена новая вещь id{} пользователя id{}.", id, item.getOwner().getId());
        id++;
        return item;
    }

    public Optional<Item> getItemById(long id) {
        log.info("Запрошена вещь id{}.");
        return Optional.ofNullable(items.get(id));
    }

    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        log.info("Обновлены данные вещи id{} пользователя id{}.", item.getId(), item.getOwner().getId());
        return item;
    }

    public Collection<Item> getUserItems(long userId) {
        log.info("Запрошен список вещей пользователя {}.", userId);
        return items.values()
                .stream()
                .filter(i -> i.getOwner().getId() == userId)
                .collect(Collectors.toList());
    }

    public Collection<Item> searchAvailableItems(String text) {
        return items.values()
                .stream()
                .filter(i -> ((i.getName().toLowerCase().contains(text))
                        || (i.getDescription().toLowerCase().contains(text)))
                        && i.getAvailable().equals(true))
                .collect(Collectors.toList());
    }
}
