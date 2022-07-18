package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.Status;

import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findBookingsByBooker_Id(long bookerId);

    Collection<Booking> findBookingsByBooker_IdAndStatus(long bookerId, Status status);

    @Query (value = "select b from bookings b " +
            "left join items i on b.item_id = i.id " +
            "where i.owner_id = ?" +
            "and b.status = ?", nativeQuery = true)
    Collection<Booking> findBookingsByOwnerIdAndStatus(long ownerId, String status);
}
