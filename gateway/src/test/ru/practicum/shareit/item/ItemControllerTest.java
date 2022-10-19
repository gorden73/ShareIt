package ru.practicum.shareit.item;

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
import ru.practicum.shareit.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOwnerDto;
import ru.practicum.shareit.item.dto.CommentDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class ItemControllerTest {
    @MockBean
    private ItemClient client;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private UserDto user1;
    private UserDto user2;

    private ItemDto item1;

    private ItemDto item2;
    private ItemDto item3;
    private CommentDto comment;
    private BookingDto lastBooking1;
    private BookingDto nextBooking1;

    private BookingOwnerDto lastBooking2;
    private BookingOwnerDto nextBooking2;
    private ItemRequestDto request;

    @BeforeEach
    void setUp() {
        user1 = new UserDto(1L, "John1", "john.doe1@mail.com");
        user2 = new UserDto(2L, "John2", "john.doe2@mail.com");

        item1 = new ItemDto(1L, "Paper1", "Newspaper1", true, 0);

        item2 = new ItemDto(2L, "Paper2", "Newspaper2", true, 0);

        item3 = new ItemDto(3L, "Paper3", "Newspaper2", true, 1L);

        comment = new CommentDto(1L, "Great paper!", "John2", LocalDateTime.now());

        lastBooking1 = new BookingDto(1L, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().minusMinutes(1),
                Status.APPROVED, user2, item1);

        nextBooking1 = new BookingDto(2L, LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusMinutes(2),
                Status.APPROVED, user2, item1);

        lastBooking2 = new BookingOwnerDto(1L, 2L);

        nextBooking2 = new BookingOwnerDto(2L, 2L);

        request = new ItemRequestDto(1L, "I want something.", LocalDateTime.now(), List.of(item1));

        item1.setComments(List.of(comment));
    }

    @Test
    void addItemWhen200IsReturned() throws Exception {
        when(client.addItem(anyLong(), any(ItemDto.class)))
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
                .andExpect(jsonPath("$.available", is(item1.getAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())));
    }

    @Test
    void addItemWhenNameIsNullAnd400IsReturned() throws Exception {
        item1.setName(null);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addItemWhenNameIsBlankAnd400IsReturned() throws Exception {
        item1.setName("");
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addItemWhenDescriptionIsNullAnd400IsReturned() throws Exception {
        item1.setDescription(null);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addItemWhenDescriptionIsBlankAnd400IsReturned() throws Exception {
        item1.setDescription("");
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addItemWhenAvailableIsNullAnd400IsReturned() throws Exception {
        item1.setAvailable(null);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void updateItemWhen200IsReturned() throws Exception {
        when(client.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
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
                .andExpect(jsonPath("$.available", is(item1.getAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())));
    }

    @Test
    void getItemByIdByOtherUserWithLastBookingAndNextBookingAndCommentsWhen200IsReturned() throws Exception {
        when(client.getItemById(anyLong(), anyLong()))
                .thenReturn(item1);
        item1.setLastBooking(null);
        item1.setNextBooking(null);
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
                .andExpect(jsonPath("$.available", is(item1.getAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())))
                .andExpect(jsonPath("$.lastBooking", is(nullValue())))
                .andExpect(jsonPath("$.nextBooking", is(nullValue())))
                .andExpect(jsonPath("$.comments.size()", is(1)))
                .andExpect(jsonPath("$.comments.[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.comments.[0].text", is(comment.getText())))
                .andExpect(jsonPath("$.comments.[0].authorName", is(comment.getAuthorName())))
                .andExpect(jsonPath("$.comments.[0].created", is(notNullValue())));
    }

    @Test
    void getItemByIdByOwnerWithLastBookingAndNextBookingAndCommentsWhen200IsReturned() throws Exception {
        when(client.getItemById(anyLong(), anyLong()))
                .thenReturn(item1);
        item1.setLastBooking(lastBooking2);
        item1.setNextBooking(nextBooking2);
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
                .andExpect(jsonPath("$.available", is(item1.getAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())))
                .andExpect(jsonPath("$.lastBooking", is(notNullValue())))
                .andExpect(jsonPath("$.lastBooking.id", is(lastBooking2.getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(lastBooking2.getBookerId()), Long.class))
                .andExpect(jsonPath("$.nextBooking", is(notNullValue())))
                .andExpect(jsonPath("$.nextBooking.id", is(nextBooking2.getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(nextBooking2.getBookerId()), Long.class))
                .andExpect(jsonPath("$.comments.size()", is(1)))
                .andExpect(jsonPath("$.comments.[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.comments.[0].text", is(comment.getText())))
                .andExpect(jsonPath("$.comments.[0].authorName", is(comment.getAuthorName())))
                .andExpect(jsonPath("$.comments.[0].created", is(notNullValue())));
    }

    @Test
    void getItemByIdByOwnerWithLastBookingAndCommentsWithoutNextBookingWhen200IsReturned() throws Exception {
        when(client.getItemById(anyLong(), anyLong()))
                .thenReturn(item1);
        item1.setLastBooking(lastBooking2);
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
                .andExpect(jsonPath("$.available", is(item1.getAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())))
                .andExpect(jsonPath("$.lastBooking", is(notNullValue())))
                .andExpect(jsonPath("$.lastBooking.id", is(lastBooking2.getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(lastBooking2.getBookerId()), Long.class))
                .andExpect(jsonPath("$.nextBooking", is(nullValue())))
                .andExpect(jsonPath("$.comments.size()", is(1)))
                .andExpect(jsonPath("$.comments.[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.comments.[0].text", is(comment.getText())))
                .andExpect(jsonPath("$.comments.[0].authorName", is(comment.getAuthorName())))
                .andExpect(jsonPath("$.comments.[0].created", is(notNullValue())));
    }

    @Test
    void getGetItemByIdByOwnerWithNextBookingAndCommentsWithoutLastBookingWhen200IsReturned() throws Exception {
        when(client.getItemById(anyLong(), anyLong()))
                .thenReturn(item1);
        item1.setNextBooking(nextBooking2);
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
                .andExpect(jsonPath("$.available", is(item1.getAvailable())))
                .andExpect(jsonPath("$.requestId", is((int) item1.getRequestId())))
                .andExpect(jsonPath("$.lastBooking", is(nullValue())))
                .andExpect(jsonPath("$.nextBooking", is(notNullValue())))
                .andExpect(jsonPath("$.nextBooking.id", is(nextBooking2.getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(nextBooking2.getBookerId()), Long.class))
                .andExpect(jsonPath("$.comments.size()", is(1)))
                .andExpect(jsonPath("$.comments.[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.comments.[0].text", is(comment.getText())))
                .andExpect(jsonPath("$.comments.[0].authorName", is(comment.getAuthorName())))
                .andExpect(jsonPath("$.comments.[0].created", is(notNullValue())));
    }

    @Test
    void getOwnerItemsWhen200IsReturned() throws Exception {
        List<ItemDto> items = new ArrayList<>();
        items.add(item1);
        when(client.getOwnerItems(anyLong(), anyInt(), anyInt()))
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
                .andExpect(jsonPath("$[0].available", is(item1.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is((int) item1.getRequestId())));
    }

    @Test
    void getOwnerItemsWhenFromIsNoPositiveAnd400IsReturned() throws Exception {
        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getOwnerItemsWhenSizeEqualdsZeroAnd400IsReturned() throws Exception {
        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void searchAvailableItemsWhen200IsReturned() throws Exception {
        List<ItemDto> items = new ArrayList<>();
        items.add(item1);
        when(client.searchAvailableItems(anyString(), anyInt(), anyInt()))
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
                .andExpect(jsonPath("$[0].available", is(item1.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is((int) item1.getRequestId())));
    }

    @Test
    void searchAvailableItemsWhenTextIsBlankAnd200IsReturned() throws Exception {
        mvc.perform(get("/items/search")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void searchAvailableItemsWhenFromIsNoPositiveAnd400IsReturned() throws Exception {
        mvc.perform(get("/items/search")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "asga")
                        .param("from", "-1")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void searchAvailableItemsWhenSizeEqualsZeroAnd400IsReturned() throws Exception {
        mvc.perform(get("/items/search")
                        .content(mapper.writeValueAsString(item1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "asga")
                        .param("from", "0")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addCommentByItemIdWhen200IsReturned() throws Exception {
        when(client.addCommentByItemId(anyLong(), any(CommentDto.class), anyLong()))
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
                .andExpect(jsonPath("$.authorName", is(comment.getAuthorName())))
                .andExpect(jsonPath("$.created", is(notNullValue())));
    }

    @Test
    void addCommentByItemIdWhenTextIsBlank400IsReturned() throws Exception {
        comment.setText("");
        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }
}