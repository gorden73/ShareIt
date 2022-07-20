package ru.practicum.shareit.item.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingDto;

@Getter
@Setter
@EqualsAndHashCode
public class ItemOwnerDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private long request;
    private BookingDto lastBooking;
    private BookingDto nextBooking;

    public ItemOwnerDto(long id, String name, String description, Boolean available, BookingDto lastBooking,
                        BookingDto nextBooking) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.lastBooking = lastBooking;
        this.nextBooking = nextBooking;
    }
}
