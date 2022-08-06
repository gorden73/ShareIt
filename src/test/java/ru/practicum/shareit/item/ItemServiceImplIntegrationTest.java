package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.requests.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(locations = "classpath:application-test.properties")
class ItemServiceImplIntegrationTest {
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

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
        item1.setOwner(user1);

        item2 = new Item("Paper2", "Newspaper2", true, 0);
        item2.setId(2L);
        item2.setOwner(user2);

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

        item1.setComments(List.of(comment));
        item1.setRequest(request);
        item1.setLastBooking(lastBooking);
        item1.setNextBooking(nextBooking);

        userRepository.save(user1);
        userRepository.save(user1);
        requestRepository.save(request);
        itemRepository.save(item1);
        bookingRepository.save(lastBooking);
        bookingRepository.save(nextBooking);
        commentRepository.save(comment);
    }

    @Test
    void getOwnerItems() {
        Collection<Item> items = itemService.getOwnerItems(1L, 0, 1);
        Item returnedItem = items.stream().findFirst().get();
        assertThat(items.size(), equalTo(1));
        assertThat(returnedItem.getId(), equalTo(item1.getId()));
        assertThat(returnedItem.getName(), equalTo(item1.getName()));
        assertThat(returnedItem.getDescription(), equalTo(item1.getDescription()));
        assertThat(returnedItem.getIsAvailable(), equalTo(item1.getIsAvailable()));
        assertThat(returnedItem.getLastBooking(), equalTo(item1.getLastBooking()));
        assertThat(returnedItem.getNextBooking(), equalTo(item1.getNextBooking()));
        assertThat(returnedItem.getComments().size(), equalTo(1));
        assertThat(returnedItem.getComments().get(0).getText(), equalTo(comment.getText()));
        assertThat(returnedItem.getRequestId(), equalTo(item1.getRequest().getId()));
    }
}