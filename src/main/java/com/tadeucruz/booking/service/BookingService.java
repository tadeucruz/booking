package com.tadeucruz.booking.service;

import static com.tadeucruz.booking.enums.BookingStatus.ACTIVATED;
import static com.tadeucruz.booking.enums.ServiceLockTypes.RESERVATIONS;
import static java.time.temporal.ChronoUnit.DAYS;

import com.tadeucruz.booking.config.BookingConfig;
import com.tadeucruz.booking.exception.BookingConflictException;
import com.tadeucruz.booking.exception.BookingNotFoundException;
import com.tadeucruz.booking.model.db.Booking;
import com.tadeucruz.booking.model.rest.BookingAvailabilityResponse;
import com.tadeucruz.booking.repository.BookingRepository;
import com.tadeucruz.booking.repository.ServiceLockRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceLockRepository serviceLockRepository;
    private final RoomService roomService;
    private final MessageSourceService messageSourceService;
    private final BookingConfig bookingConfig;


    public List<BookingAvailabilityResponse> getFreeTimes(Integer roomId) {

        var result = new ArrayList<BookingAvailabilityResponse>();

        var today = LocalDate.now().atStartOfDay();

        var dayAlreadyBooked = new HashSet<LocalDate>();

        var bookings = bookingRepository.findByRoomIdAndStartDateAfter(roomId, today);

        for (Booking booking : bookings) {
            LocalDate date = booking.getStartDate().toLocalDate();

            for (int i = 0; i <= DAYS.between(booking.getStartDate(), booking.getEndDate()); i++) {
                dayAlreadyBooked.add(date);
                date = date.plusDays(1);
            }

        }

        LocalDate date = today.toLocalDate().plusDays(1);
        for (int i = 1; i < 30; i++) {

            String status = dayAlreadyBooked.contains(date) ? "BOOKED" : "FREE";

            var tmp = BookingAvailabilityResponse.builder()
                .day(date)
                .status(status)
                .build();

            date = date.plusDays(1);

            result.add(tmp);
        }

        return result;
    }

    public List<Booking> getAllBooking() {

        return bookingRepository.findAll();
    }

    public Booking getBookingById(Integer id) {
        var optionalBooking = getOptionalBookingById(id);

        if (optionalBooking.isEmpty()) {
            throw new BookingNotFoundException(
                messageSourceService.getMessage("booking.invalid.id", id));
        }

        return optionalBooking.get();
    }

    public Optional<Booking> getOptionalBookingById(Integer id) {

        return bookingRepository.findById(id);
    }

    @Transactional
    public Booking createBooking(Integer roomId, Integer userId, LocalDateTime startDate,
        LocalDateTime endTime) {

        serviceLockRepository.findByName(RESERVATIONS.name());

        roomService.checkIfRoomExistsAndEnabled(roomId);
        // TODO: Need check for userService if user exist

        checkIfBookingDatesAreValid(roomId, startDate, endTime);

        var reservation = Booking.builder()
            .roomId(roomId)
            .userId(userId)
            .startDate(startDate)
            .endDate(endTime)
            .status(ACTIVATED)
            .build();

        return bookingRepository.save(reservation);
    }

    private void checkIfBookingDatesAreValid(Integer roomId, LocalDateTime startDate,
        LocalDateTime endTime) {

        checkIfStartDateIsBeforeEndDate(startDate, endTime);
        checkIfStartDateIsValid(startDate);
        checkIfUserIsBookingDaysInRowIsMoreTheAllowedDays(startDate, endTime);
        checkIfUserIsBookingDaysInAdvanceIsMoreTheAllowedDays(startDate);
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

    private void checkIfUserIsBookingDaysInRowIsMoreTheAllowedDays(LocalDateTime startDate,
        LocalDateTime endTime) {

        var maxEndTime = startDate.plusDays(bookingConfig.getMaxDaysInRow()).plusDays(1)
            .minusSeconds(1);

        if (endTime.isAfter(maxEndTime)) {
            throw new RuntimeException("Start Date is inalid");
        }
    }

    private void checkIfUserIsBookingDaysInAdvanceIsMoreTheAllowedDays(LocalDateTime starDate) {

        var maxStartTime = LocalDate.now().atStartOfDay()
            .plusDays(bookingConfig.getMaxDaysAdvance());

        if (starDate.isAfter(maxStartTime)) {
            throw new RuntimeException("Start Date is inalid");
        }
    }

    private void checkIfStarDateAndEndDateIsAvailable(Integer roomId, LocalDateTime startDate,
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
