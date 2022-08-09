package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.requests.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
    void shouldGetItemByIdWithLastBookingAndNextBookingWhenUserIsOwner() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        when(commentRepository.findAllByItem_Id(anyLong()))
                .thenReturn(List.of(comment));
        when(bookingRepository.findBookingsByItem_Id(anyLong()))
                .thenReturn(List.of(lastBooking, nextBooking));
        item1.setComments(List.of(comment));
        item1.setOwner(user1);
        Item returnedItem = itemService.getItemById(1L, 1L);
        assertThat(returnedItem.getId(), equalTo(item1.getId()));
        assertThat(returnedItem.getName(), equalTo(item1.getName()));
        assertThat(returnedItem.getDescription(), equalTo(item1.getDescription()));
        assertThat(returnedItem.getIsAvailable(), equalTo(item1.getIsAvailable()));
        assertThat(returnedItem.getRequest(), nullValue());
        assertThat(returnedItem.getLastBooking().getId(), equalTo(lastBooking.getId()));
        assertThat(returnedItem.getLastBooking().getItem().getId(), equalTo(lastBooking.getItem().getId()));
        assertThat(returnedItem.getLastBooking().getBooker().getId(), equalTo(lastBooking.getBooker().getId()));
        assertThat(returnedItem.getLastBooking().getStatus(), equalTo(lastBooking.getStatus()));
        assertTrue(returnedItem.getLastBooking().getStart().truncatedTo(ChronoUnit.SECONDS)
                .isEqual(lastBooking.getStart().truncatedTo(ChronoUnit.SECONDS)));
        assertTrue(returnedItem.getLastBooking().getEnd().truncatedTo(ChronoUnit.SECONDS)
                .isEqual(lastBooking.getEnd().truncatedTo(ChronoUnit.SECONDS)));
        assertThat(returnedItem.getNextBooking().getId(), equalTo(nextBooking.getId()));
        assertThat(returnedItem.getNextBooking().getItem().getId(), equalTo(nextBooking.getItem().getId()));
        assertThat(returnedItem.getNextBooking().getBooker().getId(), equalTo(nextBooking.getBooker().getId()));
        assertThat(returnedItem.getNextBooking().getStatus(), equalTo(nextBooking.getStatus()));
        assertTrue(returnedItem.getNextBooking().getStart().truncatedTo(ChronoUnit.SECONDS)
                .isEqual(nextBooking.getStart().truncatedTo(ChronoUnit.SECONDS)));
        assertTrue(returnedItem.getNextBooking().getEnd().truncatedTo(ChronoUnit.SECONDS)
                .isEqual(nextBooking.getEnd().truncatedTo(ChronoUnit.SECONDS)));
        assertThat(returnedItem.getComments().size(), equalTo(1));
        assertThat(returnedItem.getComments().get(0).getText(), equalTo(comment.getText()));
    }

    @Test
    void shouldGetItemByIdWithoutLastBookingAndNextBookingWhenUserIsNotOwner() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        when(commentRepository.findAllByItem_Id(anyLong()))
                .thenReturn(List.of(comment));
        when(bookingRepository.findBookingsByItem_Id(anyLong()))
                .thenReturn(List.of(lastBooking, nextBooking));
        item1.setComments(List.of(comment));
        item1.setOwner(user1);
        Item returnedItem = itemService.getItemById(3L, 1L);
        assertThat(returnedItem.getId(), equalTo(item1.getId()));
        assertThat(returnedItem.getName(), equalTo(item1.getName()));
        assertThat(returnedItem.getDescription(), equalTo(item1.getDescription()));
        assertThat(returnedItem.getIsAvailable(), equalTo(item1.getIsAvailable()));
        assertThat(returnedItem.getRequest(), nullValue());
        assertThat(returnedItem.getLastBooking(), nullValue());
        assertThat(returnedItem.getNextBooking(), nullValue());
        assertThat(returnedItem.getComments().size(), equalTo(1));
        assertThat(returnedItem.getComments().get(0).getText(), equalTo(comment.getText()));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenGetItemByIdWhenItemIsNotFound() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> itemService.getItemById(1L, 1L));
        assertTrue(exception.getMessage().contains("вещь с id"));
    }

    @Test
    void shouldCheckUserByIdAndReturnVoid() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        itemService.checkUserById(1L);
        verify(userRepository, times(1)).existsById(1L);
        verifyNoMoreInteractions(itemRepository, bookingRepository, requestRepository, commentRepository);
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenCheckUserByIdWhenUserNotFound() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> itemService.checkUserById(1L));
        assertTrue(exception.getMessage().contains("Не найден пользователь с id"));
    }

    @Test
    void shouldReturnEmptyCollectionWhenGetOwnerItemsWhenUserHasNotItems() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(itemRepository.findItemsByOwnerId(anyLong(), any(Pageable.class)))
                .thenReturn(List.of());
        Collection<Item> ownerItems = itemService.getOwnerItems(anyLong(), 0, 1);
        assertTrue(ownerItems.isEmpty());
    }

    @Test
    void shouldReturnCollectionOfItemsWhenGetOwnerItemsWhenUserHasNotItems() {
        item1.setOwner(user1);
        item1.setLastBooking(lastBooking);
        item1.setNextBooking(nextBooking);
        item2.setId(2L);
        item2.setOwner(user1);
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(itemRepository.findItemsByOwnerId(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(item1, item2));
        Collection<Item> ownerItems = itemService.getOwnerItems(anyLong(), 0, 1);
        assertFalse(ownerItems.isEmpty());
        assertTrue(ownerItems.contains(item1));
        assertTrue(ownerItems.contains(item2));
        assertThat(ownerItems.size(), equalTo(2));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenGetOwnerItemsWhenUserNotFound() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> itemService.getOwnerItems(1L, 0, 1));
        assertTrue(exception.getMessage().contains("Не найден пользователь с id"));
    }

    @Test
    void shouldReturnAvailableItemsWhenSearchAvailableItems() {
        when(itemRepository.searchAvailableItems(anyString(), any(Pageable.class)))
                .thenReturn(List.of(item1));
        Collection<Item> returnedItems = itemService.searchAvailableItems("Newspaper", 0, 1);
        assertFalse(returnedItems.isEmpty());
        assertTrue(returnedItems.contains(item1));
        assertThat(returnedItems.size(), equalTo(1));
    }

    @Test
    void shouldThrowValidationExceptionWhenSearchAvailableItemsWhenFromLessThanZero() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.searchAvailableItems("Newspaper", -1, 1));
        assertTrue(exception.getMessage().contains("недопустимое значение from"));
    }

    @Test
    void shouldThrowValidationExceptionWhenSearchAvailableItemsWhenSizeLessThanOne() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.searchAvailableItems("Newspaper", 0, 0));
        assertTrue(exception.getMessage().contains("недопустимое значение size"));
    }

    @Test
    void shouldReturnEmptyCollectionWhenSearchAvailableItemsWhenRequestTextIsBlank() {
        when(itemRepository.searchAvailableItems(anyString(), any(Pageable.class)))
                .thenReturn(List.of());
        Collection<Item> returnedItems = itemService.searchAvailableItems("", 0, 1);
        assertTrue(returnedItems.isEmpty());
    }

    @Test
    void shouldAddCommentByItemId() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        when(bookingRepository.findByItem_IdAndBooker_IdAndEndBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(lastBooking));
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);
        Comment savedComment = itemService.addCommentByItemId(2L, comment, 1L);
        assertThat(savedComment.getId(), equalTo(comment.getId()));
        assertThat(savedComment.getText(), equalTo(comment.getText()));
        assertThat(savedComment.getAuthor().getId(), equalTo(comment.getAuthor().getId()));
        assertThat(savedComment.getAuthor().getName(), equalTo(comment.getAuthor().getName()));
        assertThat(savedComment.getAuthor().getEmail(), equalTo(comment.getAuthor().getEmail()));
        assertThat(savedComment.getItem().getId(), equalTo(comment.getItem().getId()));
        assertThat(savedComment.getItem().getName(), equalTo(comment.getItem().getName()));
        assertThat(savedComment.getItem().getDescription(), equalTo(comment.getItem().getDescription()));
        assertThat(savedComment.getItem().getIsAvailable(), equalTo(comment.getItem().getIsAvailable()));
        assertThat(savedComment.getItem().getRequest(), nullValue());
    }

    @Test
    void shouldThrowValidationExceptionWhenAddCommentByItemIdWhenTextIsBlank() {
        comment.setText(" ");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addCommentByItemId(2L, comment, 1L));
        assertTrue(exception.getMessage().contains("отзыв пустой или состоит из пробелов."));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenAddCommentByItemIdWhenUserIsNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> itemService.addCommentByItemId(3L, comment, 1L));
        assertTrue(exception.getMessage().contains("пользователь с id3 не найден."));
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenAddCommentByItemIdWhenItemIsNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> itemService.addCommentByItemId(2L, comment, 3L));
        assertTrue(exception.getMessage().contains("вещь с id"));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddCommentByItemIdWhenBookingIsNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item1));
        when(bookingRepository.findByItem_IdAndBooker_IdAndEndBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of());
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addCommentByItemId(3L, comment, 1L));
        assertTrue(exception.getMessage().contains("пользователь id3 не может оставить отзыв на вещь "));
    }

    @Test
    void shouldReturnCollectionOfItemsWhenSearchAvailableItemsByRequestId() {
        item1.setRequest(request);
        when(itemRepository.searchAvailableItemsByRequest_Id(anyLong()))
                .thenReturn(List.of(item1));
        Collection<Item> availableItems = itemService.searchAvailableItemsByRequestId(1L);
        assertFalse(availableItems.isEmpty());
        assertThat(availableItems.size(), equalTo(1));
        assertTrue(availableItems.contains(item1));
    }
}