package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.requests.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
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

    private final ItemRequestRepository requestRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository,
                           ItemRequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public Item addItem(long userId, Item item) {
        checkInputDataByAddItem(item);
        User user = userRepository.findById(userId).orElseThrow(() -> new ElementNotFoundException(
                String.format("пользователь с таким id%d.", userId)));
        item.setOwner(user);
        if (item.getRequestId() > 0) {
            requestRepository.findById(item.getRequestId()).ifPresent(item::setRequest);
        }
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
        Item item = itemRepository.findById(id).orElseThrow(() -> new ElementNotFoundException(
                String.format("вещь с id%d.", id)));
        if (item.getOwner().getId() == userId) {
            log.info(String.format("Запрошена вещь с id%d", id));
            return addIntoItemLastAndNextBookings(addCommentsIntoItem(item));
        }
        log.info(String.format("Запрошена вещь с id%d", id));
        return addCommentsIntoItem(item);
    }

    private Item addCommentsIntoItem(Item item) {
        List<Comment> comments = commentRepository.findAllByItem_Id(item.getId());
        item.setComments(comments);
        return item;
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

    public void checkUserById(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ElementNotFoundException(String.format("пользователь с id%d.", userId));
        }
    }

    @Override
    public Collection<Item> getOwnerItems(long ownerId, int from, int size) {
        Pageable page = PageRequest.of(from, size);
        checkUserById(ownerId);
        log.info("Запрошен список вещей пользователя {}.", ownerId);
        Collection<Item> ownerItems = itemRepository.findItemsByOwnerId(ownerId, page);
        if (ownerItems.isEmpty()) {
            return List.of();
        }
        return ownerItems.stream()
                .map(this::addCommentsIntoItem)
                .map(this::addIntoItemLastAndNextBookings)
                .sorted(Comparator.comparing(Item::getId))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> searchAvailableItems(String text, int from, int size) {
        Pageable page = checkPageBorders(from, size);
        if (text.isBlank()) {
            log.info("Пустой поисковый запрос.");
            return List.of();
        }
        log.info("Поиск вещей по запросу - {}.", text);
        return itemRepository.searchAvailableItems(text, page);
    }

    @Override
    public Comment addCommentByItemId(long bookerId, Comment comment, long itemId) {
        if (comment.getText().isBlank()) {
            log.error("Отзыв пустой или состоит из пробелов.");
            throw new ValidationException("отзыв пустой или состоит из пробелов.");
        }
        User booker = userRepository.findById(bookerId).orElseThrow(() -> new ElementNotFoundException(
                String.format("пользователь с id%d не найден.", bookerId)));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ElementNotFoundException(String.format(
                "вещь с id%d.", itemId)));
        Collection<Booking> booking = bookingRepository.findByItem_IdAndBooker_IdAndEndBefore(itemId, bookerId,
                LocalDateTime.now());
        if (booking.isEmpty()) {
            log.error("Пользователь id{} не может оставить отзыв на вещь id{}.", bookerId, itemId);
            throw new ValidationException(String.format("пользователь id%d не может оставить отзыв на вещь " +
                    "id%d.", bookerId, itemId));
        }
        comment.setAuthor(booker);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        log.info("Добавлен новый комментарий к вещи id{}.", itemId);
        return commentRepository.save(comment);
    }

    private Pageable checkPageBorders(int from, int size) {
        if (from < 0) {
            throw new ValidationException(String.format("неверное значение from %d.", from));
        }
        if (size < 1) {
            throw new ValidationException(String.format("неверное значение size %d.", size));
        }
        return PageRequest.of(from, size);
    }

    @Override
    public List<Item> searchAvailableItemsByRequestId(long requestId) {
        return itemRepository.searchAvailableItemsByRequest_Id(requestId);
    }
}
