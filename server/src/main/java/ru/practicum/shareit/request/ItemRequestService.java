package ru.practicum.shareit.request;

import java.util.Collection;
import java.util.List;

public interface ItemRequestService {
    ItemRequest addRequest(long userId, ItemRequest request);

    Collection<ItemRequest> getAllItemRequestsByOwner(long userId);

    List<ItemRequest> getAllItemRequestsByOtherUsers(long userId, int from, int size);

    ItemRequest getItemRequestById(long userId, long requestId);
}
