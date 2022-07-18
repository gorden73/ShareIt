package ru.practicum.shareit.booking.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
public class BookingDto {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private long item;
    private long booker;
    private Status status;

    public BookingDto(LocalDateTime start, LocalDateTime end, long item) {
        this.start = start;
        this.end = end;
        this.item = item;
    }

    public BookingDto(long id, LocalDateTime start, LocalDateTime end, long item, long booker, Status status) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.item = item;
        this.booker = booker;
        this.status = status;
    }
}
