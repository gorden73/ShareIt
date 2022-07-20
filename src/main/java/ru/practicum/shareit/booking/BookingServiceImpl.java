package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.Status;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    UserRepository userRepository;
    ItemRepository itemRepository;
    BookingRepository bookingRepository;

    @Autowired
    public BookingServiceImpl(UserRepository userRepository, ItemRepository itemRepository,
                              BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Booking addBooking(long userId, Booking booking) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.error("Не найден пользователь с id{}.", userId);
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", userId));
        }
        Optional<Item> item = itemRepository.findById(booking.getItemId());
        if (item.isEmpty()) {
            log.error("Не найдена вещь с id{}.", booking.getItemId());
            throw new ElementNotFoundException(String.format("вещь с id%d.", booking.getItemId()));
        }
        booking.setItem(item.get());
        if (user.get().equals(booking.getItem().getOwner())) {
            log.error("Владелец вещи не может арендовать сам у себя.");
            throw new ElementNotFoundException("Владелец вещи не может арендовать сам у себя.");
        }
        booking.setBooker(user.get());
        if (!booking.getItem().getIsAvailable()) {
            log.error("Бронирование вещи id{} недоступно.", booking.getItem().getId());
            throw new ValidationException(String.format("бронирование вещи id%d недоступно.",
                    booking.getItem().getId()));
        }
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            log.error("Время начала бронирования в прошлом.");
            throw new ValidationException("время начала бронирования в прошлом.");
        }
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            log.error("Время окончания бронирования в прошлом.");
            throw new ValidationException("время окончания бронирования в прошлом.");
        }
        booking.setStatus(Status.WAITING);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking setApprovedByOwner(long userId, long bookingId, boolean approved) {
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            log.error("Не найден пользователь с id{}.", userId);
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", userId));
        }
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            log.error("Не найдено бронирование с id{}.", bookingId);
            throw new ElementNotFoundException(String.format("бронирование с таким id%d.", bookingId));
        }
        Booking booking1 = booking.get();
        if (booking1.getItem().getOwner().getId() != userId) {
            if (booking1.getBooker().getId() == userId) {
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
            if (booking1.getStatus().equals(Status.APPROVED)) {
                log.error("Повторное изменение статуса на идентичный не допускается.");
                throw new ValidationException("Повторное изменение статуса на идентичный не допускается.");
            }
            booking1.setStatus(Status.APPROVED);
            log.info("Подтверждено бронирование id{} вещи id{}.", bookingId, booking1.getItem().getId());
        } else {
            if (booking1.getStatus().equals(Status.REJECTED)) {
                log.error("Повторное изменение статуса на идентичный не допускается.");
                throw new ValidationException("Повторное изменение статуса на идентичный не допускается.");
            }
            booking1.setStatus(Status.REJECTED);
            log.info("Отклонено бронирование id{} вещи id{}.", bookingId, booking1.getItem().getId());
        }
        return bookingRepository.save(booking1);
    }

    @Override
    public Booking getBookingById(long userId, long bookingId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (user.isEmpty()) {
            log.error("Не найден пользователь с id{}.", userId);
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", userId));
        }
        if (booking.isEmpty()) {
            log.error("Не найдено бронирование с id{}.", bookingId);
            throw new ElementNotFoundException(String.format("бронирование с таким id%d.", bookingId));
        }
        Booking booking1 = booking.get();
        if (booking1.getBooker().getId() == userId || booking1.getItem().getOwner().getId() == userId) {
            return booking1;
        } else {
            throw new ElementNotFoundException(String.format("пользователь id%d не является владельцем или " +
                    "арендатором вещи.", userId));
        }
    }

    @Override
    public Collection<Booking> getAllBookingsByUserId(long bookerId, String state) {
        Optional<User> user = userRepository.findById(bookerId);
        if (user.isEmpty()) {
            log.error("Не найден пользователь с id{}.", bookerId);
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", bookerId));
        }
        if (state.equals("ALL")) {
            return bookingRepository.findBookingsByBooker_Id(bookerId);
        }
        return bookingRepository.findBookingsByBooker_IdAndStatus(bookerId, Status.valueOf(state));
    }

    @Override
    public Collection<Booking> getAllBookingsByOwnerId(long ownerId, String status) {
        Optional<User> user = userRepository.findById(ownerId);
        String status1 = status.toUpperCase();
        if (user.isEmpty()) {
            log.error("Не найден пользователь с id{}.", ownerId);
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", ownerId));
        }
        if (status1.equals("ALL")) {
            log.info("Запрошен список всех бронирований пользователя id{}.", ownerId);
            return bookingRepository.findBookingsByOwnerId(ownerId);
        } else {
            if (!List.of("CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED").contains(status1)) {
                log.error("Введено неверное значение статуса {}.", status1);
                throw new ValidationException("значение статуса может быть только CURRENT, PAST, FUTURE, WAITING, " +
                        "REJECTED");
            }
            log.info("Запрошен список бронирований пользователя id{} со статусом {}.", ownerId, status1);
            return bookingRepository.findBookingsByOwnerIdAndStatus(ownerId, Status.valueOf(status1));
        }
    }
}
