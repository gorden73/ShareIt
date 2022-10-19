package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
@ContextConfiguration(classes = ShareItGateway.class)
class ItemRequestControllerTest {
    @MockBean
    private ItemRequestClient client;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private UserDto user1;
    private UserDto user2;

    private ItemDto item1;

    private ItemDto item2;

    private ItemRequestDto request1;

    @BeforeEach
    void setUp() {
        user1 = new UserDto(1L, "John1", "john.doe1@mail.com");
        user2 = new UserDto(2L, "John2", "john.doe2@mail.com");

        item1 = new ItemDto(1L, "Paper1", "Newspaper1", true, 0);

        item2 = new ItemDto(2L, "Paper2", "Newspaper2", true, 1);

        request1 = new ItemRequestDto(1L, "Want something.", LocalDateTime.now(), null);
    }

    @Test
    void addRequestWhen200IsReturned() throws Exception {
        when(client.addRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(request1);
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
    void addRequestWhenDescriptionInNullAnd400IsReturned() throws Exception {
        request1.setDescription(null);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addRequestWhenDescriptionInBlankAnd400IsReturned() throws Exception {
        request1.setDescription("");
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllItemRequestsByOwnerWhen200IsReturned() throws Exception {
        List<ItemRequestDto> requests = new ArrayList<>();
        requests.add(request1);
        when(client.getAllItemRequestsByOwner(anyLong()))
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
    void getAllItemRequestsByOtherUsersWhen200IsReturned() throws Exception {
        List<ItemRequestDto> requests = new ArrayList<>();
        requests.add(request1);
        when(client.getAllItemRequestsByOtherUsers(anyLong(), anyInt(), anyInt()))
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
    void getAllItemRequestsByOtherUsersWhenFromIsNoPositiveAnd400IsReturned() throws Exception {
        List<ItemRequestDto> requests = new ArrayList<>();
        requests.add(request1);
        when(client.getAllItemRequestsByOtherUsers(anyLong(), anyInt(), anyInt()))
                .thenThrow(ValidationException.class);
        mvc.perform(get("/requests/all")
                        .content(mapper.writeValueAsString(requests))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", "-1")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllItemRequestsByOtherUsersWhenSizeEqualsZeroAnd400IsReturned() throws Exception {
        List<ItemRequestDto> requests = new ArrayList<>();
        requests.add(request1);
        when(client.getAllItemRequestsByOtherUsers(anyLong(), anyInt(), anyInt()))
                .thenThrow(ValidationException.class);
        mvc.perform(get("/requests/all")
                        .content(mapper.writeValueAsString(requests))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", "0")
                        .param("size", "0")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getItemRequestByIdWhen200IsReturned() throws Exception {
        when(client.getItemRequestById(anyLong(), anyLong()))
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
}