package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Item addItem(long userId, Item item) {
        checkInputDataByAddItem(item);
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", userId));
        }
        item.setOwner(user.get());
        log.info("Добавлена новая вещь {} пользователя id{}.", item, item.getOwner().getId());
        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(long userId, long itemId, Item updatedItem) {
        checkUserById(userId);
        Item item = getItemById(itemId);
        if (item.getOwner().getId() != userId) {
            throw new ElementNotFoundException(String.format("вещь id%d у пользователя с id%d.", item.getId(), userId));
        }
        if (updatedItem.getName() != null) {
            if (!updatedItem.getName().isBlank()) {
                item.setName(updatedItem.getName());
            }
        }
        if (updatedItem.getDescription() != null) {
            if (!updatedItem.getDescription().isBlank()) {
                item.setDescription(updatedItem.getDescription());
            }
        }
        if (updatedItem.getAvailable() != null) {
            item.setAvailable(updatedItem.getAvailable());
        }
        log.info("Обновлены данные вещи id{} пользователя id{}.", item.getId(), item.getOwner().getId());
        return itemRepository.save(item);
    }

    @Override
    public Item getItemById(long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            log.error("Вещь с id{} не найдена.", id);
            throw new ElementNotFoundException(String.format("вещь с id%d.", id));
        }
        log.info(String.format("Запрошена вещь с id%d", id));
        return item.get();
    }

    private void checkInputDataByAddItem(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("item.Name = null или item.Name состоит из пробелов");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("item.Description = null");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("item.isAvailable = null");
        }
    }

    private void checkUserById(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ElementNotFoundException(String.format("пользователь с id%d.", userId));
        }
    }

    @Override
    public Collection<Item> getUserItems(long userId) {
        checkUserById(userId);
        log.info("Запрошен список вещей пользователя {}.", userId);
        return itemRepository.findItemsByOwnerId(userId);
    }

    @Override
    public Collection<Item> searchAvailableItems(String text) {
        log.info("Поиск вещей по запросу - {}.", text);
        return itemRepository.searchAvailableItems(text.toLowerCase());
    }
}
