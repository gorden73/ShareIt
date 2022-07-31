package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.Status;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findBookingsByBooker_Id(long bookerId);

    Collection<Booking> findBookingsByBooker_IdAndStatus(long bookerId, Status status);

    @Query("select b from Booking b " +
            "left join Item i on b.item.id = i.id " +
            "where i.owner.id = ?1")
    Collection<Booking> findBookingsByOwnerId(long ownerId);

    @Query("select b from Booking b " +
            "left join Item i on b.item.id = i.id " +
            "where i.owner.id = ?1 " +
            "and b.status = ?2")
    Collection<Booking> findBookingsByOwnerIdAndStatus(long ownerId, Status status);

    Collection<Booking> findBookingsByItem_Id(long itemId);

    Collection<Booking> findByItem_IdAndBooker_IdAndEndBefore(long itemId, long bookerId, LocalDateTime time);

    Collection<Booking> findByBooker_IdAndEndBefore(long bookerId, LocalDateTime time);

    Collection<Booking> findByBooker_IdAndStartBeforeAndEndAfter(long bookerId, LocalDateTime eqStart,
                                                                 LocalDateTime eqEnd);

    Collection<Booking> findByBooker_IdAndStartAfter(long bookerId, LocalDateTime time);

    @Query("select b from Booking b " +
            "left join Item i on b.item.id = i.id " +
            "where i.owner.id = ?1 " +
            "and b.end < ?2")
    Collection<Booking> findByOwner_IdAndEndBefore(long ownerId, LocalDateTime time);

    @Query("select b from Booking b " +
            "left join Item i on b.item.id = i.id " +
            "where i.owner.id = ?1 " +
            "and b.start < ?2 " +
            "and b.end > ?3")
    Collection<Booking> findByOwner_IdAndStartBeforeAndEndAfter(long ownerId, LocalDateTime eqStart,
                                                                LocalDateTime eqEnd);

    @Query("select b from Booking b " +
            "left join Item i on b.item.id = i.id " +
            "where i.owner.id = ?1 " +
            "and b.start > ?2")
    Collection<Booking> findByOwner_IdAndStartAfter(long ownerId, LocalDateTime time);
}
