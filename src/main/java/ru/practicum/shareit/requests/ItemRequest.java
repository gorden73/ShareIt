package ru.practicum.shareit.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "item_requests")
@NoArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "description", nullable = false, length = 1000)
    private String description;
    @JoinColumn(name = "requester_id", nullable = false)
    @ManyToOne
    private User requester;
    private LocalDateTime created;
    @Transient
    private List<ItemOwnerDto> items;

    public ItemRequest(String description) {
        this.description = description;
        this.created = LocalDateTime.now();
    }
}
