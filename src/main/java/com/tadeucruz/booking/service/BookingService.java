package com.tadeucruz.booking.service;

import static com.tadeucruz.booking.enums.ServiceLockTypes.RESERVATIONS;

import com.tadeucruz.booking.exception.BookingConflictException;
import com.tadeucruz.booking.exception.BookingNotFoundException;
import com.tadeucruz.booking.model.db.Booking;
import com.tadeucruz.booking.repository.BookingRepository;
import com.tadeucruz.booking.repository.ServiceLockRepository;
import java.time.LocalDate;
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

    private final MessageSourceService messageSourceService;


    public List<Booking> getAllBooking() {

        return bookingRepository.findAll();
    }

    public Booking getBookingById(UUID id) {
        var optionalBooking = getOptionalBookingById(id);

        if (optionalBooking.isEmpty()) {
            throw new BookingNotFoundException(
                messageSourceService.getMessage("booking.invalid.id", id));
        }

        return optionalBooking.get();
    }

    public Optional<Booking> getOptionalBookingById(UUID id) {

        return bookingRepository.findById(id);
    }

    @Transactional
    public Booking createBooking(UUID userId, LocalDateTime startDate,
        LocalDateTime endTime) {

        return createBooking(UUID.fromString("9d83e3a0-4599-4984-8675-4a0d14ba29fa"), userId,
            startDate, endTime);
    }

    @Transactional
    public Booking createBooking(UUID roomId, UUID userId, LocalDateTime startDate,
        LocalDateTime endTime) {

        serviceLockRepository.findByName(RESERVATIONS.name());

        checkIfBookingDatesAreValid(roomId, startDate, endTime);

        var reservation = Booking.builder()
            .id(UUID.randomUUID())
            .roomId(roomId)
            .userId(userId)
            .startDate(startDate)
            .endDate(endTime)
            .build();

        return bookingRepository.save(reservation);
    }

    private void checkIfBookingDatesAreValid(UUID roomId, LocalDateTime startDate,
        LocalDateTime endTime) {

        checkIfStartDateIsBeforeEndDate(startDate, endTime);
        checkIfStartDateIsValid(startDate);
        checkIfStarDateAndEndDateIsAvailable(roomId, startDate, endTime);

    }

    private void checkIfStartDateIsBeforeEndDate(LocalDateTime startDate, LocalDateTime endTime) {

        if (startDate.isAfter(endTime)) {
            throw new RuntimeException("Start Date is inalid");
        }

    }

    private void checkIfStartDateIsValid(LocalDateTime startDate) {

        var tomorrow = LocalDate.now().atStartOfDay().plusDays(1);

        if (startDate.isBefore(tomorrow)) {
            throw new RuntimeException("Start Date is inalid");
        }
    }

    private void checkIfStarDateAndEndDateIsAvailable(UUID roomId, LocalDateTime startDate,
        LocalDateTime endTime) {

        var bookingsBetweenStartDate = bookingRepository.findByRoomIdAndStartDateBetween(roomId,
            startDate, endTime);
        var bookingsBetweenEndDate = bookingRepository.findByRoomIdAndEndDateBetween(roomId,
            startDate, endTime);
        var bookingBetweenDates = bookingRepository.findByRoomIdAndBetweenStartDateAndEndDate(
            roomId, startDate, endTime);

        if (!bookingsBetweenStartDate.isEmpty() || !bookingsBetweenEndDate.isEmpty()
            || !bookingBetweenDates.isEmpty()) {

            throw new BookingConflictException(
                messageSourceService.getMessage("booking.date.conflict"));
        }
    }


}
