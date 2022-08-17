package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBookingsByBooker_Id(long bookerId, Pageable pageable);

    List<Booking> findBookingsByBooker_IdAndStatus(long bookerId, Status status, Pageable pageable);

    @Query("select b from Booking b " +
            "left join Item i on b.item.id = i.id " +
            "where i.owner.id = ?1")
    List<Booking> findBookingsByOwnerId(long ownerId, Pageable pageable);

    @Query("select b from Booking b " +
            "left join Item i on b.item.id = i.id " +
            "where i.owner.id = ?1 " +
            "and b.status = ?2")
    List<Booking> findBookingsByOwnerIdAndStatus(long ownerId, Status status, Pageable pageable);

    List<Booking> findBookingsByItem_Id(long itemId);

    List<Booking> findByItem_IdAndBooker_IdAndEndBefore(long itemId, long bookerId, LocalDateTime time);

    List<Booking> findByBooker_IdAndEndBefore(long bookerId, LocalDateTime time, Pageable pageable);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfter(long bookerId, LocalDateTime eqStart,
                                                           LocalDateTime eqEnd, Pageable pageable);

    List<Booking> findByBooker_IdAndStartAfter(long bookerId, LocalDateTime time, Pageable pageable);

    @Query("select b from Booking b " +
            "left join Item i on b.item.id = i.id " +
            "where i.owner.id = ?1 " +
            "and b.end < ?2")
    List<Booking> findByOwner_IdAndEndBefore(long ownerId, LocalDateTime time, Pageable pageable);

    @Query("select b from Booking b " +
            "left join Item i on b.item.id = i.id " +
            "where i.owner.id = ?1 " +
            "and b.start < ?2 " +
            "and b.end > ?3")
    List<Booking> findByOwner_IdAndStartBeforeAndEndAfter(long ownerId, LocalDateTime eqStart,
                                                          LocalDateTime eqEnd, Pageable pageable);

    @Query("select b from Booking b " +
            "left join Item i on b.item.id = i.id " +
            "where i.owner.id = ?1 " +
            "and b.start > ?2")
    List<Booking> findByOwner_IdAndStartAfter(long ownerId, LocalDateTime time, Pageable pageable);
}
