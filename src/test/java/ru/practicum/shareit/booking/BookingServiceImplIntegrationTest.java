package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(locations = "classpath:application-test.properties")
class BookingServiceImplIntegrationTest {
    private final BookingService bookingService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private Item item1;
    private Item item2;
    private User user1;
    private User user2;
    private Booking booking1;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "John1", "doe1@mail.ru");
        user2 = new User(2L, "John2", "john.doe2@mail.com");

        item1 = new Item("Thing", "Cool thing", true, 1);
        item1.setId(1L);
        item1.setOwner(user1);

        item2 = new Item("Paper2", "Newspaper2", true, 0);
        item2.setId(2L);
        item2.setOwner(user2);

        booking1 = new Booking(LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusMinutes(2), 1L);

        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @Test
    void shouldAddBooking() {
        Booking savedBooking = bookingService.addBooking(2L, booking1);
        assertThat(savedBooking.getId(), notNullValue());
        assertThat(savedBooking.getItem().getId(), equalTo(item1.getId()));
        assertThat(savedBooking.getItem().getName(), equalTo(item1.getName()));
        assertThat(savedBooking.getItem().getDescription(), equalTo(item1.getDescription()));
        assertThat(savedBooking.getItem().getIsAvailable(), equalTo(item1.getIsAvailable()));
        assertThat(savedBooking.getBooker().getId(), equalTo(user2.getId()));
        assertThat(savedBooking.getBooker().getName(), equalTo(user2.getName()));
        assertThat(savedBooking.getBooker().getEmail(), equalTo(user2.getEmail()));
        assertThat(savedBooking.getStart(), notNullValue());
        assertThat(savedBooking.getEnd(), notNullValue());
    }
}