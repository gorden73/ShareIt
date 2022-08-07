package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplUnitTest {
    @MockBean
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

    @Test
    void addBooking() {
    }

    @Test
    void setApprovedByOwner() {
    }

    @Test
    void getBookingById() {
    }

    @Test
    void getAllBookingsByUserId() {
    }

    @Test
    void getAllBookingsByOwnerId() {
    }
}