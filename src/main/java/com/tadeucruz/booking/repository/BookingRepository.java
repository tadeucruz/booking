package com.tadeucruz.booking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tadeucruz.booking.enums.BookingStatus;
import com.tadeucruz.booking.model.db.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByRoomIdAndStatusAndStartDateBetween(Integer roomId, BookingStatus status,
            LocalDateTime startDate, LocalDateTime endDate);

    List<Booking> findByRoomIdAndStatusAndEndDateBetween(Integer roomId, BookingStatus status,
            LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT booking FROM Booking booking WHERE booking.roomId = ?1 AND booking.status = ?2 and booking.startDate <= ?3 AND booking.endDate >= ?4")
    List<Booking> findByRoomIdAndStatusAndBetweenStartDateAndEndDate(Integer roomId,
            BookingStatus status, LocalDateTime startDate, LocalDateTime endDate);

    List<Booking> findByRoomIdAndStatusAndStartDateAfter(Integer roomId, BookingStatus status,
            LocalDateTime date);

}
