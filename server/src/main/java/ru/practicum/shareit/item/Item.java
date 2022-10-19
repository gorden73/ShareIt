package ru.practicum.shareit.item;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "items")
@NoArgsConstructor
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String description;
    @Column(name = "is_available")
    private Boolean isAvailable;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    @ManyToOne
    @JoinColumn(name = "request_id")
    private ItemRequest request;
    @Transient
    private long requestId;
    @Transient
    private Booking lastBooking;
    @Transient
    private Booking nextBooking;
    @Transient
    private List<Comment> comments = new ArrayList<>();

    public Item(String name, String description, Boolean available, long requestId) {
        this.name = name;
        this.description = description;
        this.isAvailable = available;
        this.requestId = requestId;
    }
}
