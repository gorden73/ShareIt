package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
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
        Item item = getItemById(userId, itemId);
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
        if (updatedItem.getIsAvailable() != null) {
            item.setIsAvailable(updatedItem.getIsAvailable());
        }
        log.info("Обновлены данные вещи id{} пользователя id{}.", item.getId(), item.getOwner().getId());
        return itemRepository.save(item);
    }

    @Override
    public Item getItemById(long userId, long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            log.error("Вещь с id{} не найдена.", id);
            throw new ElementNotFoundException(String.format("вещь с id%d.", id));
        }
        Item item1 = item.get();
        if (item1.getOwner().getId() == userId) {
            log.info(String.format("Запрошена вещь с id%d", id));
            return addIntoItemLastAndNextBookings(item1);
        }
        log.info(String.format("Запрошена вещь с id%d", id));
        return item1;
    }

    private Item addIntoItemLastAndNextBookings(Item item) {
        Collection<Booking> itemsBookings = bookingRepository.findBookingsByItem_Id(item.getId());
        if (!itemsBookings.isEmpty()) {
            Optional<Booking> lastBooking = itemsBookings.stream()
                    .filter(i -> i.getEnd().isBefore(LocalDateTime.now()))
                    .findFirst();
            lastBooking.ifPresent(item::setLastBooking);
            Optional<Booking> nextBooking = itemsBookings.stream()
                    .filter(i -> i.getStart().isAfter(LocalDateTime.now()))
                    .findFirst();
            nextBooking.ifPresent(item::setNextBooking);
        }
        return item;
    }

    private void checkInputDataByAddItem(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("item.Name = null или item.Name состоит из пробелов");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("item.Description = null");
        }
        if (item.getIsAvailable() == null) {
            throw new ValidationException("item.isAvailable = null");
        }
    }

    private void checkUserById(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ElementNotFoundException(String.format("пользователь с id%d.", userId));
        }
    }

    @Override
    public Collection<Item> getOwnerItems(long ownerId) {
        checkUserById(ownerId);
        log.info("Запрошен список вещей пользователя {}.", ownerId);
        Collection<Item> ownerItems = itemRepository.findItemsByOwnerId(ownerId);
        if (ownerItems.isEmpty()) {
            return List.of();
        }
        return ownerItems.stream()
                .map(this::addIntoItemLastAndNextBookings)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> searchAvailableItems(String text) {
        if (text.isBlank()) {
            log.info("Пустой поисковый запрос.");
            return List.of();
        }
        log.info("Поиск вещей по запросу - {}.", text);
        return itemRepository.searchAvailableItems(text.toLowerCase());
    }

    @Override
    public Comment addCommentByItemId(Comment comment, long itemId) {
        if (comment.getText().isBlank()) {
            log.error("Отзыв пустой или состоит из пробелов.");
            throw new ValidationException("отзыв пустой или состоит из пробелов.");
        }
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            log.error("Не найдена вещь с id{}.", itemId);
            throw new ElementNotFoundException(String.format("вещь с id%d.", itemId));
        }
        comment.setItem(item.get());
        comment.setCreated(LocalDateTime.now());
        log.info("Добавлен новый комментарий к вещи id{}.", itemId);
        return commentRepository.save(comment);
    }
}
