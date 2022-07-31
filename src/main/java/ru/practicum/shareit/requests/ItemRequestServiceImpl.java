package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.Collections;
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
        if (request.getDescription().isBlank()) {
            throw new ValidationException("описание запроса пустое или состоит из пробелов.");
        }
        User requester = userRepository.findById(userId).orElseThrow(() -> new IllegalAccessError(
                String.format("пользователь с id%d.", userId)));
        request.setRequester(requester);
        log.info("Добавлен новый запрос пользователем id{}.", userId);
        return requestRepository.save(request);
    }

    @Override
    public Collection<ItemRequest> getAllItemRequestsByOwner(long userId) {
        if (!userRepository.existsById(userId)) {
            return Collections.emptyList();
        }
        log.info("Запрошен список запросов пользователя id{}.", userId);
        return requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId)
                .stream()
                .map(i -> i.setItems(itemService.searchAvailableItems(i.getDescription())
                                .stream()
                                .map(ItemMapper::toItemOwnerDto)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequest> getAllItemRequestsByOtherUsers(long requesterId, int from, int size) {
        if (from < 0) {
            throw new IllegalArgumentException(String.format("неверное значение from %d.", from));
        }
        if (size < 1) {
            throw new IllegalArgumentException(String.format("неверное значение size %d.", size));
        }
        Pageable page = PageRequest.of(from, size);
        log.info("Пользователь id{} запросил список запросов других пользователей.", requesterId);
        return requestRepository.findAllByRequesterIdIsNotOrderByCreatedDesc(requesterId, page);
    }

    @Override
    public ItemRequest getItemRequestById(long userId, long requestId) {
        itemService.checkUserById(userId);
        log.info("Пользователь id{} запросил запрос id{}.", userId, requestId);
        ItemRequest itemRequest = requestRepository.findById(requestId).orElseThrow(() ->
                new ElementNotFoundException(String.format("запрос с таким id%d.", requestId)));

        return setItems(itemService.searchAvailableItems());
    }
}
