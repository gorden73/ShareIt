package ru.practicum.shareit.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String text;
    @ManyToOne
    @Column(name = "item_id")
    private Item item;
    @ManyToOne
    @Column(name = "author_id")
    private User author;
    private LocalDateTime created;

    public Comment() {
    }

    public Comment(String text) {
        this.text = text;
    }
}
