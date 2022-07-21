package ru.practicum.shareit.booking.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class BookingOwnerDto {
    private long id;
    private long bookerId;

    public BookingOwnerDto() {
    }

    public BookingOwnerDto(long id, long bookerId) {
        this.id = id;
        this.bookerId = bookerId;
    }
}
