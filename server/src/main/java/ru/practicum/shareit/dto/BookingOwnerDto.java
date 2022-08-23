package ru.practicum.shareit.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class BookingOwnerDto {
    private long id;
    private long bookerId;

    public BookingOwnerDto(long id, long bookerId) {
        this.id = id;
        this.bookerId = bookerId;
    }
}
