package ru.practicum.shareit.requests;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    Collection<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(long requesterId);

    Optional<ItemRequest> findById(long requestId);

    List<ItemRequest> findAllByRequesterIdIsNotOrderByCreatedDesc(long requesterId, Pageable pageable);
}
