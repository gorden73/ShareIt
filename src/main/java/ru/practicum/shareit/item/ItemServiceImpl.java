package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item addItem(long userId, Item item) {
        checkUserById(userId);
        checkInputDataByAddItem(item);
        User user = userRepository.getUserById(userId).get();
        item.setOwner(user);
        return itemRepository.addItem(item);
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
        return itemRepository.updateItem(item);
    }

    @Override
    public Item getItemById(long id) {
        Optional<Item> item = itemRepository.getItemById(id);
        if (item.isEmpty()) {
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
        if (!userRepository.checkUserById(userId)) {
            throw new ElementNotFoundException(String.format("пользователь с id%d.", userId));
        }
    }

    @Override
    public Collection<Item> getUserItems(long userId) {
        checkUserById(userId);
        return itemRepository.getUserItems(userId);
    }
}
