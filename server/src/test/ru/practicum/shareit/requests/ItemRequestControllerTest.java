package ru.practicum.shareit.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.requests.ItemRequestController;
import ru.practicum.shareit.requests.ItemRequestService;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @MockBean
    private ItemRequestService service;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private User user1;
    private User user2;

    private Item item1;

    private Item item2;

    private ItemRequest request1;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "John1", "john.doe1@mail.com");
        user2 = new User(2L, "John2", "john.doe2@mail.com");

        item1 = new Item("Paper1", "Newspaper1", true, 0);
        item1.setId(1L);
        item1.setOwner(user1);

        item2 = new Item("Paper2", "Newspaper2", true, 1);
        item2.setId(2L);
        item2.setOwner(user2);

        request1 = new ItemRequest("Want something.");
        request1.setId(1L);
    }

    @Test
    void addRequestWhen200IsReturned() throws Exception {
        when(service.addRequest(anyLong(), any(ItemRequest.class)))
                .thenReturn(request1);
        request1.setRequester(user1);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(request1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(request1.getDescription())))
                .andExpect(jsonPath("$.created", is(notNullValue())))
                .andExpect(jsonPath("$.items", is(nullValue())));
    }

    @Test
    void addRequestWhen400IsReturned() throws Exception {
        when(service.addRequest(anyLong(), any(ItemRequest.class)))
                .thenThrow(ValidationException.class);
        request1.setRequester(user1);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addRequestWhen500IsReturned() throws Exception {
        when(service.addRequest(anyLong(), any(ItemRequest.class)))
                .thenThrow(IllegalAccessError.class);
        request1.setRequester(user1);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500));
    }

    @Test
    void getAllItemRequestsByOwnerWhen200IsReturned() throws Exception {
        request1.setRequester(user1);
        List<ItemRequest> requests = new ArrayList<>();
        requests.add(request1);
        when(service.getAllItemRequestsByOwner(anyLong()))
                .thenReturn(requests);
        mvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(requests))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(requests.size())))
                .andExpect(jsonPath("$.[0].id", is(request1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(request1.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(notNullValue())))
                .andExpect(jsonPath("$.[0].items", is(nullValue())));
    }

    @Test
    void getAllItemRequestsByOwnerWhen404IsReturned() throws Exception {
        request1.setRequester(user1);
        List<ItemRequest> requests = new ArrayList<>();
        requests.add(request1);
        when(service.getAllItemRequestsByOwner(anyLong()))
                .thenThrow(ElementNotFoundException.class);
        mvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(requests))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getAllItemRequestsByOtherUsersWhen200IsReturned() throws Exception {
        request1.setRequester(user1);
        List<ItemRequest> requests = new ArrayList<>();
        requests.add(request1);
        when(service.getAllItemRequestsByOtherUsers(anyLong(), anyInt(), anyInt()))
                .thenReturn(requests);
        mvc.perform(get("/requests/all")
                        .content(mapper.writeValueAsString(requests))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", "0")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(requests.size())))
                .andExpect(jsonPath("$.[0].id", is(request1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(request1.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(notNullValue())))
                .andExpect(jsonPath("$.[0].items", is(nullValue())));
    }

    @Test
    void getAllItemRequestsByOtherUsersWhen400IsReturned() throws Exception {
        request1.setRequester(user1);
        List<ItemRequest> requests = new ArrayList<>();
        requests.add(request1);
        when(service.getAllItemRequestsByOtherUsers(anyLong(), anyInt(), anyInt()))
                .thenThrow(ValidationException.class);
        mvc.perform(get("/requests/all")
                        .content(mapper.writeValueAsString(requests))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", "0")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getItemRequestByIdWhen200IsReturned() throws Exception {
        request1.setRequester(user1);
        when(service.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(request1);
        mvc.perform(get("/requests/1")
                        .content(mapper.writeValueAsString(request1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(request1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(request1.getDescription())))
                .andExpect(jsonPath("$.created", is(notNullValue())))
                .andExpect(jsonPath("$.items", is(nullValue())));
    }

    @Test
    void getItemRequestByIdWhen404IsReturned() throws Exception {
        request1.setRequester(user1);
        when(service.getItemRequestById(anyLong(), anyLong()))
                .thenThrow(ElementNotFoundException.class);
        mvc.perform(get("/requests/1")
                        .content(mapper.writeValueAsString(request1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }
}