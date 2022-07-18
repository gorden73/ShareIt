package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.EmailAlreadyExistsException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.Status;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    UserRepository userRepository;
    BookingRepository bookingRepository;

    @Autowired
    public BookingServiceImpl(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Booking addBooking(long userId, Booking booking) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", userId));
        }
        if (user.get().equals(booking.getItem().getOwner())) {
            throw new EmailAlreadyExistsException("Владелец не может быть арендатором.");
        }
        booking.setBooker(user.get());
        booking.setStatus(Status.WAITING);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking setApprovedByOwner(long userId, long bookingId, boolean approved) {
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", userId));
        }
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            throw new ElementNotFoundException(String.format("бронирование с таким id%d.", bookingId));
        }
        Booking booking1 = booking.get();
        if (booking1.getItem().getOwner().getId() != userId) {
            throw new ValidationException(String.format("пользователь id%d не может подтверждать/отклонять " +
                            "бронирование.", userId));
        }
        if (approved) {
            booking1.setStatus(Status.APPROVED);
        } else {
            booking1.setStatus(Status.REJECTED);
        }
        return bookingRepository.save(booking1);
    }

    @Override
    public Booking getBookingById(long userId, long bookingId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (user.isEmpty()) {
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", userId));
        }
        if (booking.isEmpty()) {
            throw new ElementNotFoundException(String.format("бронирование с таким id%d.", bookingId));
        }
        if (booking.get().getBooker().getId() != userId || booking.get().getItem().getOwner().getId() != userId) {
            throw new ElementNotFoundException(String.format("пользователь id%d не является владельцем или " +
                    "арендатором вещи.", userId));
        }
        return booking.get();
    }

    @Override
    public Collection<Booking> getAllBookingsByUserId(long userId, String state) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", userId));
        }
        if (state.equals("ALL")) {
            return bookingRepository.findBookingsByBooker_Id(userId);
        }
        return bookingRepository.findBookingsByBooker_IdAndStatus(userId, state);
    }

    @Override
    public Collection<Booking> getAllBookingsByOwnerId(long userId, String state) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ElementNotFoundException(String.format("пользователь с таким id%d.", userId));
        }
        return bookingRepository.findBookingsByOwnerId(userId, state);
    }
}
