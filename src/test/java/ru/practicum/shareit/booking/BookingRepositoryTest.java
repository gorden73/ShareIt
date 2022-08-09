package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.Status;
import ru.practicum.shareit.user.User;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class BookingRepositoryTest {
    @Autowired
    private TestEntityManager em;
    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private Booking booking1;
    private Booking booking2;
    private Booking booking3;

    @BeforeEach
    void setUp() {
        user1 = new User("John1", "item1@mail.ru");
        user1.setId(1L);
        em.merge(user1);
        user2 = new User("John2", "item2@mail.ru");
        user2.setId(2L);
        em.merge(user2);
        item1 = new Item("Thing1", "Best thing1", true, 0);
        item1.setId(1L);
        item1.setOwner(user1);
        em.merge(item1);
        item2 = new Item("Thing2", "Cool thing2", true, 0);
        item2.setId(2L);
        item2.setOwner(user2);
        em.merge(item2);
        booking1 = new Booking(LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusMinutes(2), 1L);
        booking1.setId(1L);
        booking1.setItem(item1);
        booking1.setBooker(user2);
        booking1.setStatus(Status.WAITING);
        em.merge(booking1);
        booking2 = new Booking(LocalDateTime.now().plusMinutes(3), LocalDateTime.now().plusMinutes(4), 2L);
        booking2.setId(2L);
        booking2.setItem(item2);
        booking2.setBooker(user1);
        booking2.setStatus(Status.APPROVED);
        em.merge(booking2);
        booking3 = new Booking(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(6), 2L);
        booking3.setId(3L);
        booking3.setItem(item2);
        booking3.setBooker(user1);
        booking3.setStatus(Status.REJECTED);
        em.merge(booking3);
    }

    @Test
    void findBookingsByOwnerId() {
        TypedQuery<Booking> query = em.getEntityManager().createQuery("select b from Booking b " +
                "left join Item i on b.item.id = i.id " +
                "where i.owner.id = :id", Booking.class);
        List<Booking> ownerBookings = query.setParameter("id", user1.getId()).getResultList();
        assertThat(ownerBookings, hasSize(1));
        for (Booking ownerBooking : ownerBookings) {
            assertThat(ownerBooking.getId(), notNullValue());
            assertThat(ownerBooking.getStart(), notNullValue());
            assertThat(ownerBooking.getEnd(), notNullValue());
            assertThat(ownerBooking.getBooker().getId(), equalTo(user2.getId()));
            assertThat(ownerBooking.getItem().getId(), equalTo(item1.getId()));
            assertThat(ownerBooking.getStatus(), equalTo(booking1.getStatus()));
        }
    }

    @Test
    void findBookingsByOwnerIdAndStatus() {
        TypedQuery<Booking> query = em.getEntityManager().createQuery("select b from Booking b " +
                "left join Item i on b.item.id = i.id " +
                "where i.owner.id = :id " +
                "and b.status = :status", Booking.class);
        List<Booking> ownerBookings = query.setParameter("id", user2.getId())
                .setParameter("status", Status.APPROVED).getResultList();
        assertThat(ownerBookings, hasSize(1));
        for (Booking ownerBooking : ownerBookings) {
            assertThat(ownerBooking.getId(), notNullValue());
            assertThat(ownerBooking.getStart(), notNullValue());
            assertThat(ownerBooking.getEnd(), notNullValue());
            assertThat(ownerBooking.getBooker().getId(), equalTo(user1.getId()));
            assertThat(ownerBooking.getItem().getId(), equalTo(item2.getId()));
            assertThat(ownerBooking.getStatus(), equalTo(booking2.getStatus()));
        }
    }

    @Test
    void findByOwner_IdAndEndBefore() {
        booking2.setStart(LocalDateTime.now().minusMinutes(2));
        booking2.setEnd(LocalDateTime.now().minusMinutes(1));
        em.merge(booking2);
        TypedQuery<Booking> query = em.getEntityManager().createQuery("select b from Booking b " +
                "left join Item i on b.item.id = i.id " +
                "where i.owner.id = :id " +
                "and b.end < :time", Booking.class);
        List<Booking> ownerBookings = query.setParameter("id", user2.getId())
                .setParameter("time", LocalDateTime.now()).getResultList();
        assertThat(ownerBookings, hasSize(1));
        for (Booking ownerBooking : ownerBookings) {
            assertThat(ownerBooking.getId(), notNullValue());
            assertThat(ownerBooking.getStart(), notNullValue());
            assertThat(ownerBooking.getEnd(), notNullValue());
            assertThat(ownerBooking.getBooker().getId(), equalTo(user1.getId()));
            assertThat(ownerBooking.getItem().getId(), equalTo(item2.getId()));
            assertThat(ownerBooking.getStatus(), equalTo(booking2.getStatus()));
        }
    }

    @Test
    void findByOwner_IdAndStartBeforeAndEndAfter() {
        booking2.setStart(LocalDateTime.now().minusMinutes(2));
        booking2.setEnd(LocalDateTime.now().plusMinutes(1));
        em.merge(booking2);
        TypedQuery<Booking> query = em.getEntityManager().createQuery("select b from Booking b " +
                "left join Item i on b.item.id = i.id " +
                "where i.owner.id = :id " +
                "and b.start < :time1 " +
                "and b.end > :time2", Booking.class);
        List<Booking> ownerBookings = query.setParameter("id", user2.getId())
                .setParameter("time1", LocalDateTime.now())
                .setParameter("time2", LocalDateTime.now()).getResultList();
        assertThat(ownerBookings, hasSize(1));
        for (Booking ownerBooking : ownerBookings) {
            assertThat(ownerBooking.getId(), notNullValue());
            assertThat(ownerBooking.getStart(), notNullValue());
            assertThat(ownerBooking.getEnd(), notNullValue());
            assertThat(ownerBooking.getBooker().getId(), equalTo(user1.getId()));
            assertThat(ownerBooking.getItem().getId(), equalTo(item2.getId()));
            assertThat(ownerBooking.getStatus(), equalTo(booking2.getStatus()));
        }
    }

    @Test
    void findByOwner_IdAndStartAfter() {
        booking3.setStart(LocalDateTime.now().minusMinutes(2));
        booking3.setEnd(LocalDateTime.now().plusMinutes(1));
        em.merge(booking3);
        TypedQuery<Booking> query = em.getEntityManager().createQuery("select b from Booking b " +
                "left join Item i on b.item.id = i.id " +
                "where i.owner.id = :id " +
                "and b.start > :time", Booking.class);
        List<Booking> ownerBookings = query.setParameter("id", user2.getId())
                .setParameter("time", LocalDateTime.now()).getResultList();
        assertThat(ownerBookings, hasSize(1));
        for (Booking ownerBooking : ownerBookings) {
            assertThat(ownerBooking.getId(), notNullValue());
            assertThat(ownerBooking.getStart(), notNullValue());
            assertThat(ownerBooking.getEnd(), notNullValue());
            assertThat(ownerBooking.getBooker().getId(), equalTo(user1.getId()));
            assertThat(ownerBooking.getItem().getId(), equalTo(item2.getId()));
            assertThat(ownerBooking.getStatus(), equalTo(booking2.getStatus()));
        }
    }
}