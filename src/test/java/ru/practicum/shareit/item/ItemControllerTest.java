package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @MockBean
    private ItemService service;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private User user1;
    private User user2;

    private Item item1;

    private Item item2;
    private Item item3;
    private Comment comment;
    private Booking lastBooking;
    private Booking nextBooking;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        user1 = new User(1L,"John1","john.doe1@mail.com");
        user2 = new User(2L,"John2","john.doe2@mail.com");

        item1 = new Item("Paper1", "Newspaper1", true, 0);
        item1.setId(1L);
        item1.setOwner(user1);

        item2 = new Item("Paper2", "Newspaper2", true, 0);
        item2.setId(2L);
        item2.setOwner(user2);

        item3 = new Item("Paper3", "Newspaper2", true, 1L);
        item3.setId(3L);
        item3.setOwner(user2);

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
        request.setItems(List.of(ItemMapper.toItemOwnerDto(item1)));

        item1.setComments(List.of(comment));
        item1.setRequest(request);
    }

    @Test
    void addItemWhen200IsReturned() throws Exception {
        when(service.addItem(anyLong(), any(Item.class)))
                .thenReturn(item1);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item1.getName())))
                .andExpect(jsonPath("$.description", is(item1.getDescription())))
                .andExpect(jsonPath("$.available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())));
    }

    @Test
    void addItemWhen400IsReturned() throws Exception {
        when(service.addItem(anyLong(), any(Item.class)))
                .thenThrow(ValidationException.class);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addItemWhen404IsReturned() throws Exception {
        when(service.addItem(anyLong(), any(Item.class)))
                .thenThrow(ElementNotFoundException.class);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void updateItemWhen200IsReturned() throws Exception {
        when(service.updateItem(anyLong(), anyLong(), any(Item.class)))
                .thenReturn(item1);
        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item1.getName())))
                .andExpect(jsonPath("$.description", is(item1.getDescription())))
                .andExpect(jsonPath("$.available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())));
    }

    @Test
    void updateItemWhen404IsReturned() throws Exception {
        when(service.updateItem(anyLong(), anyLong(), any(Item.class)))
                .thenThrow(ElementNotFoundException.class);
        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getItemByIdByOtherUserWithLastBookingAndNextBookingAndCommentsWhen200IsReturned() throws Exception {
        when(service.getItemById(anyLong(), anyLong()))
                .thenReturn(item1);
        item1.setLastBooking(lastBooking);
        item1.setNextBooking(nextBooking);
        mvc.perform(get("/items/1")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item1.getName())))
                .andExpect(jsonPath("$.description", is(item1.getDescription())))
                .andExpect(jsonPath("$.available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())))
                .andExpect(jsonPath("$.lastBooking", is(nullValue())))
                .andExpect(jsonPath("$.nextBooking", is(nullValue())))
                .andExpect(jsonPath("$.comments.size()", is(1)))
                .andExpect(jsonPath("$.comments.[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.comments.[0].text", is(comment.getText())))
                .andExpect(jsonPath("$.comments.[0].authorName", is(comment.getAuthor().getName())))
                .andExpect(jsonPath("$.comments.[0].created", is(notNullValue())));
    }

    @Test
    void getItemByIdWhen404IsReturned() throws Exception {
        when(service.getItemById(anyLong(), anyLong()))
                .thenThrow(ElementNotFoundException.class);
        item1.setLastBooking(lastBooking);
        item1.setNextBooking(nextBooking);
        mvc.perform(get("/items/1")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getItemByIdByOwnerWithLastBookingAndNextBookingAndCommentsWhen200IsReturned() throws Exception {
        when(service.getItemById(anyLong(), anyLong()))
                .thenReturn(item1);
        item1.setLastBooking(lastBooking);
        item1.setNextBooking(nextBooking);
        mvc.perform(get("/items/1")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item1.getName())))
                .andExpect(jsonPath("$.description", is(item1.getDescription())))
                .andExpect(jsonPath("$.available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())))
                .andExpect(jsonPath("$.lastBooking", is(notNullValue())))
                .andExpect(jsonPath("$.lastBooking.id", is(lastBooking.getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(lastBooking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking", is(notNullValue())))
                .andExpect(jsonPath("$.nextBooking.id", is(nextBooking.getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(nextBooking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.comments.size()", is(1)))
                .andExpect(jsonPath("$.comments.[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.comments.[0].text", is(comment.getText())))
                .andExpect(jsonPath("$.comments.[0].authorName", is(comment.getAuthor().getName())))
                .andExpect(jsonPath("$.comments.[0].created", is(notNullValue())));
    }

    @Test
    void getItemByIdByOwnerWithLastBookingAndCommentsWithoutNextBookingWhen200IsReturned() throws Exception {
        when(service.getItemById(anyLong(), anyLong()))
                .thenReturn(item1);
        item1.setLastBooking(lastBooking);
        mvc.perform(get("/items/1")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item1.getName())))
                .andExpect(jsonPath("$.description", is(item1.getDescription())))
                .andExpect(jsonPath("$.available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())))
                .andExpect(jsonPath("$.lastBooking", is(notNullValue())))
                .andExpect(jsonPath("$.lastBooking.id", is(lastBooking.getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(lastBooking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking", is(nullValue())))
                .andExpect(jsonPath("$.comments.size()", is(1)))
                .andExpect(jsonPath("$.comments.[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.comments.[0].text", is(comment.getText())))
                .andExpect(jsonPath("$.comments.[0].authorName", is(comment.getAuthor().getName())))
                .andExpect(jsonPath("$.comments.[0].created", is(notNullValue())));
    }

    @Test
    void getGetItemByIdByOwnerWithNextBookingAndCommentsWithoutLastBookingWhen200IsReturned() throws Exception {
        when(service.getItemById(anyLong(), anyLong()))
                .thenReturn(item1);
        item1.setNextBooking(nextBooking);
        mvc.perform(get("/items/1")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item1.getName())))
                .andExpect(jsonPath("$.description", is(item1.getDescription())))
                .andExpect(jsonPath("$.available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())))
                .andExpect(jsonPath("$.lastBooking", is(nullValue())))
                .andExpect(jsonPath("$.nextBooking", is(notNullValue())))
                .andExpect(jsonPath("$.nextBooking.id", is(nextBooking.getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(nextBooking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.comments.size()", is(1)))
                .andExpect(jsonPath("$.comments.[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.comments.[0].text", is(comment.getText())))
                .andExpect(jsonPath("$.comments.[0].authorName", is(comment.getAuthor().getName())))
                .andExpect(jsonPath("$.comments.[0].created", is(notNullValue())));
    }

    @Test
    void getOwnerItemsWhen200IsReturned() throws Exception {
        List<Item> items = new ArrayList<>();
        items.add(item1);
        when(service.getOwnerItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(items);
        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(items.size())))
                .andExpect(jsonPath("$[0].id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item1.getName())))
                .andExpect(jsonPath("$[0].description", is(item1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$[0].requestId", is((int) item1.getRequestId())));
    }

    @Test
    void getOwnerItemsWhen404IsReturned() throws Exception {
        List<Item> items = new ArrayList<>();
        items.add(item1);
        when(service.getOwnerItems(anyLong(), anyInt(), anyInt()))
                .thenThrow(ElementNotFoundException.class);
        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void searchAvailableItemsWhen200IsReturned() throws Exception {
        List<Item> items = new ArrayList<>();
        items.add(item1);
        when(service.searchAvailableItems(anyString(), anyInt(), anyInt()))
                .thenReturn(items);
        mvc.perform(get("/items/search")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "Great paper!")
                        .param("from", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(items.size())))
                .andExpect(jsonPath("$[0].id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item1.getName())))
                .andExpect(jsonPath("$[0].description", is(item1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item1.getIsAvailable())))
                .andExpect(jsonPath("$[0].requestId", is((int) item1.getRequestId())));
    }

    @Test
    void addCommentByItemIdWhen200IsReturned() throws Exception {
        when(service.addCommentByItemId(anyLong(), any(Comment.class), anyLong()))
                .thenReturn(comment);
        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(comment.getText())))
                .andExpect(jsonPath("$.authorName", is(comment.getAuthor().getName())))
                .andExpect(jsonPath("$.created", is(notNullValue())));
    }

    @Test
    void addCommentByItemIdWhen400IsReturned() throws Exception {
        when(service.addCommentByItemId(anyLong(), any(Comment.class), anyLong()))
                .thenThrow(ValidationException.class);
        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addCommentByItemIdWhen404IsReturned() throws Exception {
        when(service.addCommentByItemId(anyLong(), any(Comment.class), anyLong()))
                .thenThrow(ElementNotFoundException.class);
        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }
}