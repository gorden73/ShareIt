package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;

@Component
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getIsAvailable()
        );
    }

    public static ItemOwnerDto toItemOwnerDto(Item item) {
        ItemOwnerDto dto = new ItemOwnerDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getIsAvailable()
        );
        if (item.getRequest() != null) {
            dto.setRequest(item.getRequest().getId());
        }
        if (item.getLastBooking() == null && item.getNextBooking() != null) {
            dto.setNextBooking(BookingMapper.toBookingOwnerDto(item.getNextBooking()));
            return dto;
        } else if (item.getLastBooking() != null && item.getNextBooking() == null) {
            dto.setLastBooking(BookingMapper.toBookingOwnerDto(item.getLastBooking()));
            return dto;
        } else if (item.getLastBooking() == null && item.getNextBooking() == null) {
            return dto;
        } else {
            dto.setLastBooking(BookingMapper.toBookingOwnerDto(item.getLastBooking()));
            dto.setNextBooking(BookingMapper.toBookingOwnerDto(item.getNextBooking()));
            return dto;
        }
    }

    public static Item toItem(ItemDto itemDto) {
        return new Item(
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable()
        );
    }

    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getItem(),
                comment.getAuthor(),
                comment.getCreated());
    }

    public static Comment toComment(CommentDto dto) {
        return new Comment(
                dto.getText());
    }
}
