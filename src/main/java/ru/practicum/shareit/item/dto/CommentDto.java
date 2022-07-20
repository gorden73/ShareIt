package ru.practicum.shareit.item.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
public class CommentDto {
    private long id;
    private String text;
    private Item item;
    private User author;
    private LocalDateTime created;

    public CommentDto(long id, String text, Item item, User author, LocalDateTime created) {
        this.id = id;
        this.text = text;
        this.item = item;
        this.author = author;
        this.created = created;
    }
}
