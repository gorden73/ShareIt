package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;

import java.awt.print.Book;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem().getId(),
                booking.getBooker().getId(),
                booking.getStatus());
    }

    public static Booking toBooking(BookingDto dto) {
        return new Booking(
                dto.getStart(),
                dto.getEnd(),
                dto.getItem().getId()
        )
    }
}
