package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.Status;

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
}
