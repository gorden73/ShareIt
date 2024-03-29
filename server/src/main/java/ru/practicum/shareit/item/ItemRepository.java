package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select i from Item as i " +
            "where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%'))) " +
            "and i.isAvailable = true")
    List<Item> searchAvailableItems(String text, Pageable pageable);

    List<Item> searchAvailableItemsByRequest_Id(long requestId);

    List<Item> findItemsByOwnerId(long ownerId, Pageable pageable);
}
