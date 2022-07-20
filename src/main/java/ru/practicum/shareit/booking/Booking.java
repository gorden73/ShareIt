package ru.practicum.shareit.booking;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.Status;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "start_date_time")
    private LocalDateTime start;
    @Column(name = "end_date_time")
    private LocalDateTime end;
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
    @Transient
    private long itemId;
    @ManyToOne
    @JoinColumn(name = "booker_id")
    private User booker;
    @Enumerated(EnumType.STRING)
    private Status status;

    public Booking() {
    }

    public Booking(LocalDateTime start, LocalDateTime end, long itemId) {
        this.start = start;
        this.end = end;
        this.itemId = itemId;
    }
}
