package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.requests.ItemRequestRepository;
import ru.practicum.shareit.requests.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(locations = "classpath:application-test.properties")
class ItemRequestServiceImplIntegrationTest {

    private final ItemRequestService requestService;
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private Item item1;
    private Item item2;
    private User user1;
    private User user2;
    private ItemRequest request1;
    private ItemRequest request2;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "John1", "doe1@mail.ru");
        user2 = new User(2L, "John2", "john.doe2@mail.com");
        userRepository.save(user1);
        userRepository.save(user2);

        item1 = new Item("Thing", "Cool thing", true, 1);
        item1.setId(1L);
        item1.setOwner(user1);
        item2 = new Item("Paper2", "Newspaper2", true, 1);
        item2.setId(2L);
        item2.setOwner(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);

        request1 = new ItemRequest("I want something.");
        request1.setId(1L);
        request1.setRequester(user1);
        request2 = new ItemRequest("I want something else.");
        request2.setId(2L);
        request2.setRequester(user2);
        requestRepository.save(request1);
        requestRepository.save(request2);
    }

    @Test
    void getAllItemRequestsByOwner() {
        Collection<ItemRequest> ownerRequests = requestService.getAllItemRequestsByOwner(1L);
        assertThat(ownerRequests, hasSize(1));
        for (ItemRequest req : ownerRequests) {
            assertThat(ownerRequests, hasItem(allOf(
                    hasProperty("id", equalTo(req.getId())),
                    hasProperty("description", equalTo(req.getDescription())),
                    hasProperty("created", notNullValue()),
                    hasProperty("requester", equalTo(user1))
            )));
        }
    }
}