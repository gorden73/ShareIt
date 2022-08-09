package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.Status;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplUnitTest {
    private final BookingService bookingService;
    @MockBean
    private final ItemRepository itemRepository;
    @MockBean
    private final UserRepository userRepository;
    @MockBean
    private final BookingRepository bookingRepository;

    private Item item1;
    private Item item2;
    private User user1;
    private User user2;
    private Booking booking1;
    private Booking booking2;
    private Comment comment;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "John1", "doe1@mail.ru");
        user2 = new User(2L, "John2", "john.doe2@mail.com");

        item1 = new Item("Thing", "Cool thing", true, 0);
        item1.setId(1L);

        item2 = new Item("Paper2", "Newspaper2", true, 0);

        comment = new Comment("Great paper!");
        comment.setId(1L);
        comment.setItem(item1);
        comment.setAuthor(user2);
        comment.setCreated(LocalDateTime.now());

        booking1 = new Booking(LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusMinutes(2), 1L);
        booking1.setId(1L);
        booking1.setBooker(user2);
        booking1.setItem(item1);
        booking1.setStatus(Status.APPROVED);

        booking2 = new Booking(LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusMinutes(2), 1L);
        booking2.setId(2L);
        booking2.setBooker(user2);
        booking2.setItem(item1);
        booking2.setStatus(Status.APPROVED);

        request = new ItemRequest("I want something.");
        request.setRequester(user2);
        request.setCreated(LocalDateTime.now());
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenAddBookingWhenUserNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.addBooking(3L, booking1));
        assertTrue(exception.getMessage().contains("пользователь с таким id"));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenAddBookingWhenItemNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.addBooking(2L, booking1));
        assertTrue(exception.getMessage().contains("вещь с id"));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenAddBookingWhenUserIsOwner() {
        booking1.setBooker(user1);
        item1.setOwner(user1);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.addBooking(1L, booking1));
        assertTrue(exception.getMessage().contains("Владелец вещи не может арендовать сам у себя."));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddBookingWhenItemIsNotAvailable() {
        item1.setOwner(user1);
        item1.setIsAvailable(false);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user2));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(2L, booking1));
        assertTrue(exception.getMessage().contains("бронирование вещи id1 недоступно."));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddBookingWhenStartIsBeforeNow() {
        item1.setOwner(user1);
        booking1.setStart(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user2));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(2L, booking1));
        assertTrue(exception.getMessage().contains("время начала бронирования в прошлом."));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddBookingWhenEndIsBeforeNow() {
        item1.setOwner(user1);
        booking1.setEnd(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user2));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(2L, booking1));
        assertTrue(exception.getMessage().contains("время окончания бронирования в прошлом."));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddBookingWhenEndIsBeforeStart() {
        item1.setOwner(user1);
        booking1.setStart(LocalDateTime.now().plusMinutes(2));
        booking1.setEnd(LocalDateTime.now().plusMinutes(1));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user2));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(2L, booking1));
        assertTrue(exception.getMessage().contains("время начала бронирования позже времени окончания бронирования."));
    }

    @Test
    void shouldSetApprovedByOwner() {
        booking1.setStatus(Status.WAITING);
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking1));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking1);
        Booking returnedBooking = bookingService.setApprovedByOwner(1L, 1L, true);
        assertThat(returnedBooking.getId(), equalTo(booking1.getId()));
        assertThat(returnedBooking.getStart(), notNullValue());
        assertThat(returnedBooking.getEnd(), notNullValue());
        assertThat(returnedBooking.getItem().getId(), equalTo(booking1.getItem().getId()));
        assertThat(returnedBooking.getBooker().getId(), equalTo(booking1.getBooker().getId()));
        assertThat(returnedBooking.getStatus(), equalTo(Status.APPROVED));
    }

    @Test
    void shouldSetRejectedByOwner() {
        booking1.setStatus(Status.WAITING);
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking1));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking1);
        Booking returnedBooking = bookingService.setApprovedByOwner(1L, 1L, false);
        assertThat(returnedBooking.getId(), equalTo(booking1.getId()));
        assertThat(returnedBooking.getStart(), notNullValue());
        assertThat(returnedBooking.getEnd(), notNullValue());
        assertThat(returnedBooking.getItem().getId(), equalTo(booking1.getItem().getId()));
        assertThat(returnedBooking.getBooker().getId(), equalTo(booking1.getBooker().getId()));
        assertThat(returnedBooking.getStatus(), equalTo(Status.REJECTED));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenSetApprovedByOwnerWhenUserIsNotFound() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.setApprovedByOwner(3L, 1L, true));
        assertTrue(exception.getMessage().contains("пользователь с таким id"));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenSetApprovedByOwnerWhenBookingIsNotFound() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.setApprovedByOwner(1L, 1L, true));
        assertTrue(exception.getMessage().contains("бронирование с таким id"));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenSetApprovedByOwnerWhenBookerIsUser() {
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking1));
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.setApprovedByOwner(2L, 1L, true));
        assertTrue(exception.getMessage().contains("не имеет доступа для изменения статуса бронирования"));
    }

    @Test
    void shouldThrowValidationExceptionWhenSetApprovedByOwnerWhenUserIsNotOwner() {
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.setApprovedByOwner(3L, 1L, true));
        assertTrue(exception.getMessage().contains("пользователь id3 не имеет доступа для изменения статуса " +
                "бронирования"));
    }

    @Test
    void shouldThrowValidationExceptionWhenSetApprovedByOwnerWhenStatusIsAlreadyApproved() {
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.setApprovedByOwner(1L, 1L, true));
        assertTrue(exception.getMessage().contains("Повторное изменение статуса на идентичный не допускается."));
    }

    @Test
    void shouldThrowValidationExceptionWhenSetRejectedByOwnerWhenStatusIsAlreadyRejected() {
        item1.setOwner(user1);
        booking1.setStatus(Status.REJECTED);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.setApprovedByOwner(1L, 1L, false));
        assertTrue(exception.getMessage().contains("Повторное изменение статуса на идентичный не допускается."));
    }

    @Test
    void shouldGetBookingByIdWhenUserIsBooker() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking1));
        Booking returnedBooking = bookingService.getBookingById(2L, 1L);
        assertThat(returnedBooking.getId(), equalTo(booking1.getId()));
        assertThat(returnedBooking.getStart(), notNullValue());
        assertThat(returnedBooking.getEnd(), notNullValue());
        assertThat(returnedBooking.getItem().getId(), equalTo(booking1.getItem().getId()));
        assertThat(returnedBooking.getBooker().getId(), equalTo(booking1.getBooker().getId()));
        assertThat(returnedBooking.getStatus(), equalTo(Status.APPROVED));
    }

    @Test
    void shouldGetBookingByIdWhenUserIsOwner() {
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking1));
        Booking returnedBooking = bookingService.getBookingById(1L, 1L);
        assertThat(returnedBooking.getId(), equalTo(booking1.getId()));
        assertThat(returnedBooking.getStart(), notNullValue());
        assertThat(returnedBooking.getEnd(), notNullValue());
        assertThat(returnedBooking.getItem().getId(), equalTo(booking1.getItem().getId()));
        assertThat(returnedBooking.getBooker().getId(), equalTo(booking1.getBooker().getId()));
        assertThat(returnedBooking.getStatus(), equalTo(Status.APPROVED));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenGetBookingByIdWhenUserNotFound() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.getBookingById(1L, 1L));
        assertTrue(exception.getMessage().contains("пользователь с таким id"));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenGetBookingByIdWhenBookingNotFound() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.getBookingById(1L, 3L));
        assertTrue(exception.getMessage().contains("бронирование с таким id"));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenGetBookingByIdWhenUserIsNotBookerOrOwner() {
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking1));
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.getBookingById(3L, 1L));
        assertTrue(exception.getMessage().contains("пользователь id3 не является владельцем или арендатором вещи."));
    }

    @Test
    void shouldGetAllBookingsByUserIdWhenStatusAll() {
        item1.setOwner(user1);
        booking2.setStatus(Status.WAITING);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findBookingsByBooker_Id(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking1, booking2));
        Collection<Booking> bookings = bookingService.getAllBookingsByUserId(2L, "all", 0, 2);
        assertThat(bookings.size(), equalTo(2));
        assertTrue(bookings.contains(booking1));
        assertTrue(bookings.contains(booking2));
    }

    @Test
    void shouldThrowValidationExceptionWhenGetAllBookingsByUserIdWhenFromLessThanZero() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookingsByUserId(2L, "all", -1, 2));
        assertTrue(exception.getMessage().contains("неверное значение from"));
    }

    @Test
    void shouldThrowValidationExceptionWhenGetAllBookingsByUserIdWhenFromLessThanOne() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookingsByUserId(2L, "all", 0, 0));
        assertTrue(exception.getMessage().contains("неверное значение size"));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenGetAllBookingsByUserIdWhenUserNotFound() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.getAllBookingsByUserId(3L, "all", 0, 2));
        assertTrue(exception.getMessage().contains("пользователь с таким id"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGetAllBookingsByUserIdWhenUnsupportedStatus() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllBookingsByUserId(2L, "lala", 0, 2));
        assertTrue(exception.getMessage().contains("Unknown state"));
    }

    @Test
    void shouldGetAllBookingsByUserIdWhenStatusApproved() {
        item1.setOwner(user1);
        booking2.setStatus(Status.WAITING);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findBookingsByBooker_IdAndStatus(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking1));
        Collection<Booking> bookings = bookingService.getAllBookingsByUserId(2L, "approved", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking1));
    }

    @Test
    void shouldGetAllBookingsByUserIdWhenStatusWaiting() {
        item1.setOwner(user1);
        booking2.setStatus(Status.WAITING);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findBookingsByBooker_IdAndStatus(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking2));
        Collection<Booking> bookings = bookingService.getAllBookingsByUserId(2L, "waiting", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking2));
    }

    @Test
    void shouldGetAllBookingsByUserIdWhenStatusRejected() {
        item1.setOwner(user1);
        booking1.setStatus(Status.REJECTED);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findBookingsByBooker_IdAndStatus(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking1));
        Collection<Booking> bookings = bookingService.getAllBookingsByUserId(2L, "rejected", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking1));
    }

    @Test
    void shouldGetAllBookingsByUserIdWhenStatusCurrent() {
        item1.setOwner(user1);
        booking1.setStatus(Status.REJECTED);
        booking1.setStart(LocalDateTime.now().minusMinutes(1));
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findByBooker_IdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking1));
        Collection<Booking> bookings = bookingService.getAllBookingsByUserId(2L, "current", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking1));
    }

    @Test
    void shouldGetAllBookingsByUserIdWhenStatusPast() {
        item1.setOwner(user1);
        booking1.setStatus(Status.REJECTED);
        booking1.setStart(LocalDateTime.now().minusMinutes(2));
        booking1.setStart(LocalDateTime.now().minusMinutes(1));
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findByBooker_IdAndEndBefore(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking1));
        Collection<Booking> bookings = bookingService.getAllBookingsByUserId(2L, "past", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking1));
    }

    @Test
    void shouldGetAllBookingsByUserIdWhenStatusFuture() {
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findByBooker_IdAndStartAfter(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking1));
        Collection<Booking> bookings = bookingService.getAllBookingsByUserId(2L, "future", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking1));
    }

    @Test
    void shouldGetAllBookingsByOwnerIdWhenStatusAll() {
        item1.setOwner(user1);
        booking2.setStatus(Status.WAITING);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findBookingsByOwnerId(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking1, booking2));
        Collection<Booking> bookings = bookingService.getAllBookingsByOwnerId(1L, "all", 0, 2);
        assertThat(bookings.size(), equalTo(2));
        assertTrue(bookings.contains(booking1));
        assertTrue(bookings.contains(booking2));
    }

    @Test
    void shouldThrowValidationExceptionWhenGetAllBookingsByOwnerIdWhenFromLessThanZero() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookingsByOwnerId(2L, "all", -1, 2));
        assertTrue(exception.getMessage().contains("неверное значение from"));
    }

    @Test
    void shouldThrowValidationExceptionWhenGetAllBookingsByOwnerIdWhenFromLessThanOne() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookingsByOwnerId(2L, "all", 0, 0));
        assertTrue(exception.getMessage().contains("неверное значение size"));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenGetAllBookingsByOwnerIdWhenUserNotFound() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> bookingService.getAllBookingsByOwnerId(3L, "all", 0, 2));
        assertTrue(exception.getMessage().contains("пользователь с таким id"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGetAllBookingsByOwnerIdWhenUnsupportedStatus() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllBookingsByOwnerId(1L, "lala", 0, 2));
        assertTrue(exception.getMessage().contains("Unknown state"));
    }

    @Test
    void shouldGetAllBookingsByOwnerIdWhenStatusApproved() {
        item1.setOwner(user1);
        booking2.setStatus(Status.WAITING);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findBookingsByOwnerIdAndStatus(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking1));
        Collection<Booking> bookings = bookingService.getAllBookingsByOwnerId(1L, "approved", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking1));
    }

    @Test
    void shouldGetAllBookingsByOwnerIdWhenStatusWaiting() {
        item1.setOwner(user1);
        booking2.setStatus(Status.WAITING);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findBookingsByOwnerIdAndStatus(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking2));
        Collection<Booking> bookings = bookingService.getAllBookingsByOwnerId(1L, "waiting", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking2));
    }

    @Test
    void shouldGetAllBookingsByOwnerIdWhenStatusRejected() {
        item1.setOwner(user1);
        booking1.setStatus(Status.REJECTED);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findBookingsByOwnerIdAndStatus(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking1));
        Collection<Booking> bookings = bookingService.getAllBookingsByOwnerId(1L, "rejected", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking1));
    }

    @Test
    void shouldGetAllBookingsByOwnerIdWhenStatusCurrent() {
        item1.setOwner(user1);
        booking1.setStatus(Status.REJECTED);
        booking1.setStart(LocalDateTime.now().minusMinutes(1));
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findByOwner_IdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking1));
        Collection<Booking> bookings = bookingService.getAllBookingsByOwnerId(2L, "current", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking1));
    }

    @Test
    void shouldGetAllBookingsByOwnerIdWhenStatusPast() {
        item1.setOwner(user1);
        booking1.setStatus(Status.REJECTED);
        booking1.setStart(LocalDateTime.now().minusMinutes(2));
        booking1.setStart(LocalDateTime.now().minusMinutes(1));
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findByOwner_IdAndEndBefore(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking1));
        Collection<Booking> bookings = bookingService.getAllBookingsByOwnerId(2L, "past", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking1));
    }

    @Test
    void shouldGetAllBookingsByOwnerIdWhenStatusFuture() {
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(bookingRepository.findByOwner_IdAndStartAfter(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking1));
        Collection<Booking> bookings = bookingService.getAllBookingsByOwnerId(2L, "future", 0, 2);
        assertThat(bookings.size(), equalTo(1));
        assertTrue(bookings.contains(booking1));
    }
}