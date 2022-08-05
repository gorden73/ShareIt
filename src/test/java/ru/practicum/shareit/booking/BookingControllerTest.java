package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.Status;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @MockBean
    private BookingService service;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private User user1;
    private User user2;

    private Item item1;

    private Item item2;
    private Booking booking1;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "John1", "john.doe1@mail.com");
        user2 = new User(2L, "John2", "john.doe2@mail.com");

        item1 = new Item("Paper1", "Newspaper1", true, 0);
        item1.setId(1L);
        item1.setOwner(user1);

        item2 = new Item("Paper2", "Newspaper2", true, 0);
        item2.setId(2L);
        item2.setOwner(user2);

        booking1 = new Booking(LocalDateTime.now().minusMinutes(2), LocalDateTime.now().minusMinutes(1), 1L);
        booking1.setId(1L);
        booking1.setBooker(user2);
        booking1.setItem(item1);
        booking1.setStatus(Status.WAITING);
    }

    @Test
    void addBookingWhen200IsReturned() throws Exception {
        when(service.addBooking(anyLong(), any(Booking.class)))
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
    void addBookingWhen400IsReturned() throws Exception {
        when(service.addBooking(anyLong(), any(Booking.class)))
                .thenThrow(ValidationException.class);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void addBookingWhen404IsReturned() throws Exception {
        when(service.addBooking(anyLong(), any(Booking.class)))
                .thenThrow(ElementNotFoundException.class);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void setApprovedByOwnerWhen200IsReturned() throws Exception {
        when(service.setApprovedByOwner(anyLong(), anyLong(), anyBoolean()))
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
    void setApprovedByOwnerWhen400IsReturned() throws Exception {
        when(service.setApprovedByOwner(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(ValidationException.class);
        booking1.setStatus(Status.APPROVED);
        mvc.perform(patch("/bookings/1")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void setApprovedByOwnerWhen404IsReturned() throws Exception {
        when(service.setApprovedByOwner(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(ElementNotFoundException.class);
        booking1.setStatus(Status.APPROVED);
        mvc.perform(patch("/bookings/1")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getBookingByIdWhen200IsReturned() throws Exception {
        when(service.getBookingById(anyLong(), anyLong()))
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
    void getBookingByIdWhen404IsReturned() throws Exception {
        when(service.getBookingById(anyLong(), anyLong()))
                .thenThrow(ElementNotFoundException.class);
        mvc.perform(get("/bookings/1")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getAllBookingsByUserIdWhen200IsReturned() throws Exception {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        when(service.getAllBookingsByUserId(anyLong(), anyString(), anyInt(), anyInt()))
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
    void getAllBookingsByUserIdWhen400IsReturned() throws Exception {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        when(service.getAllBookingsByUserId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(ValidationException.class);
        mvc.perform(get("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllBookingsByUserIdWhen404IsReturned() throws Exception {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        when(service.getAllBookingsByUserId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(ElementNotFoundException.class);
        mvc.perform(get("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getAllBookingsByOwnerIdWhen200IsReturned() throws Exception {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        when(service.getAllBookingsByOwnerId(anyLong(), anyString(), anyInt(), anyInt()))
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
    void getAllBookingsByOwnerIdWhen400IsReturned() throws Exception {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        when(service.getAllBookingsByOwnerId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(ValidationException.class);
        mvc.perform(get("/bookings/owner")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getAllBookingsByOwnerIdWhen404IsReturned() throws Exception {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        when(service.getAllBookingsByOwnerId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(ElementNotFoundException.class);
        mvc.perform(get("/bookings/owner")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }
}