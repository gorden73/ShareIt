package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ElementNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.Status;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public BookingServiceImpl(UserRepository userRepository, ItemRepository itemRepository,
                              BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Booking addBooking(long userId, Booking booking) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ElementNotFoundException(
                String.format("пользователь с таким id%d.", userId)));
        Item item = itemRepository.findById(booking.getItemId()).orElseThrow(() -> new ElementNotFoundException(
                String.format("вещь с id%d.", booking.getItemId())));
        if (user.equals(item.getOwner())) {
            log.error("Владелец вещи не может арендовать сам у себя.");
            throw new ElementNotFoundException("Владелец вещи не может арендовать сам у себя.");
        }
        if (!item.getIsAvailable()) {
            log.error("Бронирование вещи id{} недоступно.", item.getId());
            throw new ValidationException(String.format("бронирование вещи id%d недоступно.", item.getId()));
        }
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);
        log.info("Добавлено бронирование вещи id{}.", item.getId());
        return bookingRepository.save(booking);
    }

    @Override
    public Booking setApprovedByOwner(long userId, long bookingId, boolean approved) {
        checkUserExists(userId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ElementNotFoundException(
                String.format("бронирование с таким id%d.", bookingId)));
        if (booking.getItem().getOwner().getId() != userId) {
            if (booking.getBooker().getId() == userId) {
                log.error("Арендатор id{} не имеет доступа для изменения статуса бронирования id{}.", userId,
                        bookingId);
                throw new ElementNotFoundException(String.format("арендатор id%d не имеет доступа для изменения " +
                        "статуса бронирования id%d.", userId, bookingId));
            }
            log.error("Пользователь id{} не имеет доступа для изменения статуса бронирования id{}.", userId,
                    bookingId);
            throw new ValidationException(String.format("пользователь id%d не имеет доступа для изменения " +
                    "статуса бронирования id%d.", userId, bookingId));
        }
        if (approved) {
            if (booking.getStatus().equals(Status.APPROVED)) {
                log.error("Повторное изменение статуса на идентичный не допускается.");
                throw new ValidationException("Повторное изменение статуса на идентичный не допускается.");
            }
            booking.setStatus(Status.APPROVED);
            log.info("Подтверждено бронирование id{} вещи id{}.", bookingId, booking.getItem().getId());
        } else {
            if (booking.getStatus().equals(Status.REJECTED)) {
                log.error("Повторное изменение статуса на идентичный не допускается.");
                throw new ValidationException("Повторное изменение статуса на идентичный не допускается.");
            }
            booking.setStatus(Status.REJECTED);
            log.info("Отклонено бронирование id{} вещи id{}.", bookingId, booking.getItem().getId());
        }
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(long userId, long bookingId) {
        checkUserExists(userId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ElementNotFoundException(
                String.format("бронирование с таким id%d.", bookingId)));
        if (booking.getBooker().getId() == userId || booking.getItem().getOwner().getId() == userId) {
            return booking;
        } else {
            throw new ElementNotFoundException(String.format("пользователь id%d не является владельцем или " +
                    "арендатором вещи.", userId));
        }
    }

    @Override
    public Collection<Booking> getAllBookingsByUserId(long bookerId, String status, int from, int size) {
        int page1 = from / size;
        Pageable page = PageRequest.of(page1, size, Sort.by("start").descending());
        checkUserExists(bookerId);
        String status1 = status.toUpperCase();
        switch (status1) {
            case ("ALL"):
                log.info("Запрошен список всех бронирований арендатора id{}.", bookerId);
                return bookingRepository.findBookingsByBooker_Id(bookerId, page);
            case ("CURRENT"):
                log.info("Запрошен список бронирований арендатора id{} со статусом CURRENT.", bookerId);
                return bookingRepository.findByBooker_IdAndStartBeforeAndEndAfter(bookerId, LocalDateTime.now(),
                        LocalDateTime.now(), page);
            case ("PAST"):
                log.info("Запрошен список бронирований арендатора id{} со статусом PAST.", bookerId);
                return bookingRepository.findByBooker_IdAndEndBefore(bookerId, LocalDateTime.now(), page);
            case ("FUTURE"):
                log.info("Запрошен список бронирований арендатора id{} со статусом FUTURE.", bookerId);
                return bookingRepository.findByBooker_IdAndStartAfter(bookerId, LocalDateTime.now(), page);
        }
        log.info("Запрошен список бронирований арендатора id{} со статусом {}.", bookerId, status1);
        return bookingRepository.findBookingsByBooker_IdAndStatus(bookerId, Status.valueOf(status1), page);
    }

    @Override
    public Collection<Booking> getAllBookingsByOwnerId(long ownerId, String status, int from, int size) {
        int page1 = from / size;
        Pageable page = PageRequest.of(page1, size, Sort.by("start").descending());
        checkUserExists(ownerId);
        String status1 = status.toUpperCase();
        switch (status1) {
            case ("ALL"):
                log.info("Запрошен список всех бронирований владельца id{}.", ownerId);
                return bookingRepository.findBookingsByOwnerId(ownerId, page);
            case ("CURRENT"):
                log.info("Запрошен список бронирований владельца id{} со статусом CURRENT.", ownerId);
                return bookingRepository.findByOwner_IdAndStartBeforeAndEndAfter(ownerId, LocalDateTime.now(),
                        LocalDateTime.now(), page);
            case ("PAST"):
                log.info("Запрошен список бронирований владельца id{} со статусом PAST.", ownerId);
                return bookingRepository.findByOwner_IdAndEndBefore(ownerId, LocalDateTime.now(), page);
            case ("FUTURE"):
                log.info("Запрошен список бронирований владельца id{} со статусом FUTURE.", ownerId);
                return bookingRepository.findByOwner_IdAndStartAfter(ownerId, LocalDateTime.now(), page);
        }
        log.info("Запрошен список бронирований владельца id{} со статусом {}.", ownerId, status1);
        return bookingRepository.findBookingsByOwnerIdAndStatus(ownerId, Status.valueOf(status1), page);
    }

    private void checkUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь id{} не найден.", userId);
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", userId));
        }
    }
}


