package ru.practicum.shareit.booking.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
public class BookingDto {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Status status;
    private long itemId;
    private UserDto booker;
    private ItemDto item;

    public BookingDto() {
    }

    public BookingDto(long id, LocalDateTime start, LocalDateTime end, Status status, UserDto booker,
                      ItemDto item) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.status = status;
        this.booker = booker;
        this.item = item;
    }
}
