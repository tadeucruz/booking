package com.tadeucruz.booking.repository;

import com.tadeucruz.booking.model.db.Booking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

  List<Booking> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);

  List<Booking> findByEndDateBetween(LocalDateTime startDate, LocalDateTime endDate);

}
