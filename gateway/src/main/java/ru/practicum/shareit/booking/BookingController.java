package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ValidationException;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public Object addBooking(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                             @RequestBody BookingDto bookingDto) {
        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            log.error("Время начала бронирования в прошлом.");
            throw new ValidationException("время начала бронирования в прошлом.");
        }
        if (bookingDto.getEnd().isBefore(LocalDateTime.now())) {
            log.error("Время окончания бронирования в прошлом.");
            throw new ValidationException("время окончания бронирования в прошлом.");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            log.error("Время начала бронирования позже времени окончания бронирования.");
            throw new ValidationException("время начала бронирования позже времени окончания бронирования.");
        }
        return bookingClient.addBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public Object setApprovedByOwner(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                                     @PathVariable long bookingId,
                                     @RequestParam(required = false) boolean approved) {
        return bookingClient.setApprovedByOwner(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public Object getBookingById(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                                 @PathVariable long bookingId) {
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public Object getAllBookingsByUserId(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                                         @RequestParam(defaultValue = "ALL") String state,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        checkPageBorders(from, size);
        String status = checkValidStatus(state);
        return bookingClient.getAllBookingsByUserId(userId, status, from, size);
    }

    @GetMapping("/owner")
    public Object getAllBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") @NotNull long userId,
                                          @RequestParam(defaultValue = "ALL") String state,
                                          @RequestParam(defaultValue = "0") int from,
                                          @RequestParam(defaultValue = "10") int size) {
        checkPageBorders(from, size);
        String status = checkValidStatus(state);
        return bookingClient.getAllBookingsByOwnerId(userId, status, from, size);
    }

    private String checkValidStatus(String status) {
        String status1 = status.toUpperCase();
        if (!List.of("ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED", "APPROVED").contains(status)) {
            log.error("Введено неверное значение статуса {}.", status);
            throw new IllegalArgumentException(String.format("Unknown state: %s", status));
        }
        return status1;
    }

    private void checkPageBorders(int from, int size) {
        if (from < 0) {
            throw new ValidationException(String.format("неверное значение from %d.", from));
        }
        if (size < 1) {
            throw new ValidationException(String.format("неверное значение size %d.", size));
        }
    }
}
