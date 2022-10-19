package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ElementNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    private final ItemService itemService;

    @Override
    public ItemRequest addRequest(long userId, ItemRequest request) {
        User requester = userRepository.findById(userId).orElseThrow(() -> new IllegalAccessError(
                String.format("пользователь с id%d.", userId)));
        request.setRequester(requester);
        log.info("Добавлен новый запрос пользователем id{}.", userId);
        return requestRepository.save(request);
    }

    @Override
    public Collection<ItemRequest> getAllItemRequestsByOwner(long userId) {
        itemService.checkUserById(userId);
        log.info("Запрошен список запросов пользователя id{}.", userId);
        return requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId)
                .stream()
                .map(this::searchItemsByRequest)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequest> getAllItemRequestsByOtherUsers(long requesterId, int from, int size) {
        itemService.checkUserById(requesterId);
        Pageable page = PageRequest.of(from, size);
        log.info("Пользователь id{} запросил список запросов других пользователей.", requesterId);
        return requestRepository.findAllByRequesterIdIsNotOrderByCreatedDesc(requesterId, page)
                .stream()
                .map(this::searchItemsByRequest)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequest getItemRequestById(long userId, long requestId) {
        itemService.checkUserById(userId);
        log.info("Пользователь id{} запросил запрос id{}.", userId, requestId);
        ItemRequest itemRequest = requestRepository.findById(requestId).orElseThrow(() ->
                new ElementNotFoundException(String.format("запрос с таким id%d.", requestId)));
        return searchItemsByRequest(itemRequest);
    }

    private ItemRequest searchItemsByRequest(ItemRequest request) {
        List<ItemOwnerDto> items = itemService.searchAvailableItemsByRequestId(request.getId())
                .stream()
                .map(ItemMapper::toItemOwnerDto)
                .collect(Collectors.toList());
        request.setItems(items);
        return request;
    }
}
