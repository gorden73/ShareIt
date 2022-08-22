package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dto.BookingDto;
import ru.practicum.shareit.dto.BookingMapper;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @RequestBody BookingDto bookingDto) {
        Booking booking = BookingMapper.toBooking(bookingDto);
        return BookingMapper.toBookingDto(bookingService.addBooking(userId, booking));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto setApprovedByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long bookingId,
                                         @RequestParam Boolean approved) {
        return BookingMapper.toBookingDto(bookingService.setApprovedByOwner(userId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @PathVariable long bookingId) {
        return BookingMapper.toBookingDto(bookingService.getBookingById(userId, bookingId));
    }

    @GetMapping
    public Collection<BookingDto> getAllBookingsByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                         @RequestParam String state,
                                                         @RequestParam int from,
                                                         @RequestParam int size) {
        return bookingService.getAllBookingsByUserId(userId, state, from, size)
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getAllBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                          @RequestParam String state,
                                                          @RequestParam int from,
                                                          @RequestParam int size) {
        return bookingService.getAllBookingsByOwnerId(userId, state, from, size)
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}
