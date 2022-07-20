package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

public class BookingMapper {
    /*public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                booking.getBooker().getId(),
                booking.getItem().getId(),
                booking.getItem().getName()
                );
    }*/

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
