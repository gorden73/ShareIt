package ru.practicum.shareit.booking;

import java.util.Collection;

public interface BookingService {
    Booking addBooking(long userId, Booking booking);

    Booking setApprovedByOwner(long userId, long bookingId, boolean approved);

    Booking getBookingById(long userId, long bookingId);

    Collection<Booking> getAllBookingsByUserId(long userId, String state);

    Collection<Booking> getAllBookingsByOwnerId(long userId, String state);
}
