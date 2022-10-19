package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.ElementNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplUnitTest {

    private final ItemRequestService requestService;
    @MockBean
    private final ItemRequestRepository requestRepository;
    @MockBean
    private final UserRepository userRepository;
    @MockBean
    private final ItemService itemService;
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

        item1 = new Item("Thing", "Cool thing", true, 1);
        item1.setId(1L);

        item2 = new Item("Paper2", "Newspaper2", true, 1);

        request1 = new ItemRequest("I want something.");
        request1.setId(1L);
        request2 = new ItemRequest("I want something else.");
        request2.setId(2L);
    }

    @Test
    void shouldAddRequest() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user2));
        when(requestRepository.save(any(ItemRequest.class)))
                .thenReturn(request1);
        ItemRequest savedRequest = requestService.addRequest(2L, request1);
        assertThat(savedRequest.getId(), equalTo(request1.getId()));
        assertThat(savedRequest.getDescription(), equalTo(request1.getDescription()));
        assertThat(savedRequest.getRequester().getId(), equalTo(request1.getRequester().getId()));
        assertThat(savedRequest.getCreated(), notNullValue());
    }

    @Test
    void shouldThrowIllegalAccessErrorWhenAddRequestWhenUserNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        IllegalAccessError exception = assertThrows(IllegalAccessError.class,
                () -> requestService.addRequest(3L, request1));
        assertTrue(exception.getMessage().contains("пользователь с id"));
    }

    @Test
    void shouldGetAllItemRequestsByOtherUsers() {
        request1.setRequester(user2);
        List<ItemRequest> requests1 = List.of(request1);
        when(requestRepository.findAllByRequesterIdIsNotOrderByCreatedDesc(anyLong(), any(Pageable.class)))
                .thenReturn(requests1);
        Collection<ItemRequest> requests2 = requestService.getAllItemRequestsByOtherUsers(1L, 0, 2);
        assertThat(requests2, hasSize(requests1.size()));
        for (ItemRequest request : requests1) {
            assertThat(requests2, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(request.getDescription())),
                    hasProperty("created", notNullValue())
            )));
        }
        verify(itemService, times(1)).checkUserById(1L);
    }

    @Test
    void shouldGetItemRequestByIdWhenItemsNotFound() {
        request2.setRequester(user2);
        when(requestRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(request2));
        when(itemService.searchAvailableItemsByRequestId(anyLong()))
                .thenReturn(List.of());
        ItemRequest returnedRequest = requestService.getItemRequestById(2L, 2L);
        assertThat(returnedRequest.getId(), equalTo(request2.getId()));
        assertThat(returnedRequest.getDescription(), equalTo(request2.getDescription()));
        assertThat(returnedRequest.getCreated(), notNullValue());
        assertThat(returnedRequest.getRequester().getId(), equalTo(request2.getRequester().getId()));
        assertThat(returnedRequest.getItems(), hasSize(0));
        verify(itemService, times(1)).checkUserById(2L);
    }

    @Test
    void shouldGetItemRequestByIdWhenItemsAreFound() {
        request1.setRequester(user2);
        when(requestRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(request1));
        when(itemService.searchAvailableItemsByRequestId(anyLong()))
                .thenReturn(List.of(item1, item2));
        ItemRequest returnedRequest = requestService.getItemRequestById(2L, 1L);
        assertThat(returnedRequest.getId(), equalTo(request1.getId()));
        assertThat(returnedRequest.getDescription(), equalTo(request1.getDescription()));
        assertThat(returnedRequest.getCreated(), notNullValue());
        assertThat(returnedRequest.getRequester().getId(), equalTo(request1.getRequester().getId()));
        assertThat(returnedRequest.getItems(), hasSize(2));
        for (ItemOwnerDto item : returnedRequest.getItems()) {
            assertThat(returnedRequest.getItems(), hasItem(allOf(
                    hasProperty("id", equalTo(item.getId())),
                    hasProperty("name", equalTo(item.getName())),
                    hasProperty("description", equalTo(item.getDescription())),
                    hasProperty("available", equalTo(item.getAvailable())),
                    hasProperty("requestId", equalTo(item.getRequestId()))
            )));
        }
        verify(itemService, times(1)).checkUserById(2L);
    }

    @Test
    void shouldThrowElementNotFoundExceptionWhenGetItemRequestByIdWhenRequestNotFound() {
        when(requestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ElementNotFoundException exception = assertThrows(ElementNotFoundException.class,
                () -> requestService.getItemRequestById(1L, 3L));
        assertTrue(exception.getMessage().contains("запрос с таким id"));
    }
}