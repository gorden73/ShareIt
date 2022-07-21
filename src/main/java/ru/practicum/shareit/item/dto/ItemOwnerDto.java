package ru.practicum.shareit.item.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingOwnerDto;

@Getter
@Setter
@EqualsAndHashCode
public class ItemOwnerDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    //private long ownerId;
    private long request;
    private BookingOwnerDto lastBooking;
    private BookingOwnerDto nextBooking;

    public ItemOwnerDto(long id, String name, String description, Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
    }
}
