package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class BookingControllerTest {

    @MockBean
    private BookingClient client;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private UserDto user1;
    private UserDto user2;

    private ItemDto item1;

    private ItemDto item2;
    private BookingDto booking1;

    @BeforeEach
    void setUp() {
        user1 = new UserDto(1L, "John1", "john.doe1@mail.com");
        user2 = new UserDto(2L, "John2", "john.doe2@mail.com");

        item1 = new ItemDto(1L, "Paper1", "Newspaper1", true, 0);
        item1.setId(1L);

        item2 = new ItemDto(2L, "Paper2", "Newspaper2", true, 0);
        item2.setId(2L);

        booking1 = new BookingDto(1L, LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusMinutes(2),
                Status.WAITING, user2, item1);
    }

    @Test
    void addBookingWhen200IsReturned() throws Exception {
        when(client.addBooking(anyLong(), any(BookingDto.class)))
                .thenReturn(booking1);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(notNullValue())))
                .andExpect(jsonPath("$.end", is(notNullValue())))
                .andExpect(jsonPath("$.status", is(booking1.getStatus().toString())))
                .andExpect(jsonPath("$.booker", is(notNullValue())))
                .andExpect(jsonPath("$.booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(booking1.getBooker().getName())))
                .andExpect(jsonPath("$.item", is(notNullValue())))
                .andExpect(jsonPath("$.item.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(item1.getName())));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddBookingWhenStartIsBeforeNow() throws Exception {
        booking1.setStart(LocalDateTime.now().minusMinutes(1));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddBookingWhenEndIsBeforeNow() throws Exception {
        booking1.setEnd(LocalDateTime.now().minusMinutes(1));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void shouldThrowValidationExceptionWhenAddBookingWhenEndIsBeforeStart() throws Exception {
        booking1.setStart(LocalDateTime.now().plusMinutes(2));
        booking1.setEnd(LocalDateTime.now().plusMinutes(1));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void setApprovedByOwnerWhen200IsReturned() throws Exception {
        when(client.setApprovedByOwner(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(booking1);
        booking1.setStatus(Status.APPROVED);
        mvc.perform(patch("/bookings/1")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(notNullValue())))
                .andExpect(jsonPath("$.end", is(notNullValue())))
                .andExpect(jsonPath("$.status", is(booking1.getStatus().toString())))
                .andExpect(jsonPath("$.booker", is(notNullValue())))
                .andExpect(jsonPath("$.booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(booking1.getBooker().getName())))
                .andExpect(jsonPath("$.item", is(notNullValue())))
                .andExpect(jsonPath("$.item.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(item1.getName())));
    }

    @Test
    void getBookingByIdWhen200IsReturned() throws Exception {
        when(client.getBookingById(anyLong(), anyLong()))
                .thenReturn(booking1);
        mvc.perform(get("/bookings/1")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(notNullValue())))
                .andExpect(jsonPath("$.end", is(notNullValue())))
                .andExpect(jsonPath("$.status", is(booking1.getStatus().toString())))
                .andExpect(jsonPath("$.booker", is(notNullValue())))
                .andExpect(jsonPath("$.booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(booking1.getBooker().getName())))
                .andExpect(jsonPath("$.item", is(notNullValue())))
                .andExpect(jsonPath("$.item.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(item1.getName())));
    }

    @Test
    void getAllBookingsByUserIdWhen200IsReturned() throws Exception {
        List<BookingDto> bookings = new ArrayList<>();
        bookings.add(booking1);
        when(client.getAllBookingsByUserId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(bookings);
        mvc.perform(get("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(bookings.size())))
                .andExpect(jsonPath("$.[0].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(notNullValue())))
                .andExpect(jsonPath("$.[0].end", is(notNullValue())))
                .andExpect(jsonPath("$.[0].status", is(booking1.getStatus().toString())))
                .andExpect(jsonPath("$.[0].booker", is(notNullValue())))
                .andExpect(jsonPath("$.[0].booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.[0].booker.name", is(booking1.getBooker().getName())))
                .andExpect(jsonPath("$.[0].item", is(notNullValue())))
                .andExpect(jsonPath("$.[0].item.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.name", is(item1.getName())));
    }

    @Test
    void getAllBookingsByUserIdWhenFromEqualsNoPositiveAnd400IsReturned() throws Exception {
        when(client.getAllBookingsByUserId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(ValidationException.class);
        mvc.perform(get("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllBookingsByUserIdWhenSizeEqualsZeroAnd400IsReturned() throws Exception {
        when(client.getAllBookingsByUserId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(ValidationException.class);
        mvc.perform(get("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllBookingsByUserIdWhen500IsReturned() throws Exception {
        when(client.getAllBookingsByOwnerId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(IllegalArgumentException.class);
        mvc.perform(get("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "LALA")
                        .param("from", "0")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500));
    }

    @Test
    void getAllBookingsByOwnerIdWhen200IsReturned() throws Exception {
        List<BookingDto> bookings = new ArrayList<>();
        bookings.add(booking1);
        when(client.getAllBookingsByOwnerId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(bookings);
        mvc.perform(get("/bookings/owner")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(bookings.size())))
                .andExpect(jsonPath("$.[0].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(notNullValue())))
                .andExpect(jsonPath("$.[0].end", is(notNullValue())))
                .andExpect(jsonPath("$.[0].status", is(booking1.getStatus().toString())))
                .andExpect(jsonPath("$.[0].booker", is(notNullValue())))
                .andExpect(jsonPath("$.[0].booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.[0].booker.name", is(booking1.getBooker().getName())))
                .andExpect(jsonPath("$.[0].item", is(notNullValue())))
                .andExpect(jsonPath("$.[0].item.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.name", is(item1.getName())));
    }

    @Test
    void getAllBookingsByOwnerIdWhenFromEqualsNoPositiveAnd400IsReturned() throws Exception {
        when(client.getAllBookingsByUserId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(ValidationException.class);
        mvc.perform(get("/bookings/owner")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllBookingsByOwnerIdWhenSizeEqualsZeroAnd400IsReturned() throws Exception {
        when(client.getAllBookingsByUserId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(ValidationException.class);
        mvc.perform(get("/bookings/owner")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllBookingsByOwnerIdWhen500IsReturned() throws Exception {
        when(client.getAllBookingsByOwnerId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(IllegalArgumentException.class);
        mvc.perform(get("/bookings/owner")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "LALA")
                        .param("from", "0")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500));
    }
}