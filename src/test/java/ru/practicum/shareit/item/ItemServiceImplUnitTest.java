package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.requests.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplUnitTest {
    private final ItemService itemService;

    @MockBean
    private final ItemRepository itemRepository;
    @MockBean
    private final UserRepository userRepository;
    @MockBean
    private final BookingRepository bookingRepository;
    @MockBean
    private final CommentRepository commentRepository;
    @MockBean
    private final ItemRequestRepository requestRepository;
    private Item item1;
    private Item item2;
    private User user1;
    private User user2;
    private Comment comment;
    private Booking lastBooking;
    private Booking nextBooking;
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

        lastBooking = new Booking(LocalDateTime.now().minusMinutes(2), LocalDateTime.now().minusMinutes(1), 1L);
        lastBooking.setId(1L);
        lastBooking.setBooker(user2);
        lastBooking.setItem(item1);
        lastBooking.setStatus(Status.APPROVED);

        nextBooking = new Booking(LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusMinutes(2), 1L);
        nextBooking.setId(2L);
        nextBooking.setBooker(user2);
        nextBooking.setItem(item1);
        nextBooking.setStatus(Status.APPROVED);

        request = new ItemRequest("I want something.");
        request.setRequester(user2);
        request.setCreated(LocalDateTime.now());
    }

    @Test
    void shouldAddItem() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item1);
        Item savedItem = itemService.addItem(1L, item1);
        assertThat(savedItem.getId(), equalTo(item1.getId()));
        assertThat(savedItem.getName(), equalTo(item1.getName()));
        assertThat(savedItem.getDescription(), equalTo(item1.getDescription()));
        assertThat(savedItem.getIsAvailable(), equalTo(item1.getIsAvailable()));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddItemWithNameIsNull() {
        item1.setName(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> itemService.addItem(1L, item1));
        assertTrue(exception.getMessage().contains("item.Name = null или item.Name состоит из пробелов."));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddItemWithNameConsistsOfSpaces() {
        item1.setName("");
        ValidationException exception = assertThrows(ValidationException.class, () -> itemService.addItem(1L, item1));
        assertTrue(exception.getMessage().contains("item.Name = null или item.Name состоит из пробелов."));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddItemWithDescriptionIsNull() {
        item1.setDescription(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> itemService.addItem(1L, item1));
        assertTrue(exception.getMessage().contains("item.Description = null или состоит из пробелов."));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddItemWithDescriptionConsistsOfSpaces() {
        item1.setDescription("");
        ValidationException exception = assertThrows(ValidationException.class, () -> itemService.addItem(1L, item1));
        assertTrue(exception.getMessage().contains("item.Description = null или состоит из пробелов."));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddItemWithIsAvailableIsNull() {
        item1.setIsAvailable(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> itemService.addItem(1L, item1));
        assertTrue(exception.getMessage().contains("item.isAvailable = null."));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenAddItemWhenUserIsNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class, () -> itemService.addItem(1L
                , item1));
        assertTrue(exception.getMessage().contains("пользователь с таким id"));
    }

    @Test
    void shouldUpdateItem() {
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item2);
        Item returnedItem = itemService.updateItem(1L, 1L, item2);
        returnedItem.setId(item1.getId());
        assertThat(returnedItem.getId(), equalTo(item1.getId()));
        assertThat(item2.getName(), equalTo(returnedItem.getName()));
        assertThat(item2.getDescription(), equalTo(returnedItem.getDescription()));
        assertThat(item2.getIsAvailable(), equalTo(returnedItem.getIsAvailable()));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenUpdateItemWhenUserIsNotFound() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> itemService.updateItem(1L, 1L, item2));
        assertTrue(exception.getMessage().contains("Не найден пользователь с id"));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenUpdateItemWhenItemIsNotFound() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> itemService.updateItem(1L, 1L, item2));
        assertTrue(exception.getMessage().contains("вещь с id"));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenUpdateItemWhenUserIsNotOwner() {
        item1.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> itemService.updateItem(3L, 1L, item2));
        assertTrue(exception.getMessage().contains("вещь id1 у пользователя с id3."));
    }

    @Test
    void getItemById() {
    }

    @Test
    void checkUserById() {
    }

    @Test
    void getOwnerItems() {
    }

    @Test
    void searchAvailableItems() {
    }

    @Test
    void addCommentByItemId() {
    }

    @Test
    void searchAvailableItemsByRequestId() {
    }
}