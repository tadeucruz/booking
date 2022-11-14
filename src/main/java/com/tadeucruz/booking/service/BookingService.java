package com.tadeucruz.booking.service;

import static com.tadeucruz.booking.enums.ServiceLockTypes.RESERVATIONS;

import com.tadeucruz.booking.model.db.Booking;
import com.tadeucruz.booking.repository.BookingRepository;
import com.tadeucruz.booking.repository.ServiceLockRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceLockRepository serviceLockRepository;

    public List<Booking> getAllBooking() {

        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(UUID id) {

        return bookingRepository.findById(id);
    }

    @Transactional
    public Booking createBooking(UUID roomId, UUID userId, LocalDateTime startDate,
        LocalDateTime endTime) {

        serviceLockRepository.findByName(RESERVATIONS.name());

        if (!checkIfStarDateAndEndDateIsAvailable(startDate, endTime)) {
            throw new RuntimeException("Ja existe data");
        }

        var reservation = Booking.builder().id(UUID.randomUUID()).roomId(roomId).userId(userId)
            .startDate(startDate).endDate(endTime).build();

        bookingRepository.save(reservation);

        return null;
    }

    private boolean checkIfStarDateAndEndDateIsAvailable(LocalDateTime startDate,
        LocalDateTime endTime) {
        var bookingsBetweenStartDate = bookingRepository.findByStartDateBetween(startDate, endTime);
        var bookingsBetweenEndDate = bookingRepository.findByEndDateBetween(startDate, endTime);

        return bookingsBetweenStartDate.isEmpty() && bookingsBetweenEndDate.isEmpty();
    }


}
