package com.tadeucruz.booking.repository;

import com.tadeucruz.booking.model.db.Booking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByRoomIdAndStartDateBetween(UUID roomId, LocalDateTime startDate,
        LocalDateTime endDate);

    List<Booking> findByRoomIdAndEndDateBetween(UUID roomId, LocalDateTime startDate,
        LocalDateTime endDate);

    @Query("SELECT booking FROM Booking booking WHERE booking.roomId = ?1 AND booking.startDate <= ?2 AND booking.endDate >= ?3")
    List<Booking> findByRoomIdAndBetweenStartDateAndEndDate(UUID roomId, LocalDateTime startDate,
        LocalDateTime endDate);

}
