package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserMapper;

public class BookingMapper {
    public static BookingOwnerDto toBookingOwnerDto(Booking booking) {
        return new BookingOwnerDto(
                booking.getId(),
                booking.getBooker().getId()
        );
    }

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                UserMapper.toUserDto(booking.getBooker()),
                ItemMapper.toItemDto(booking.getItem())
        );
    }

    public static Booking toBooking(BookingDto dto) {
        return new Booking(
                dto.getStart(),
                dto.getEnd(),
                dto.getItemId()
        );
    }
}
