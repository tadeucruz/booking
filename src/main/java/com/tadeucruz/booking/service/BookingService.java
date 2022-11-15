package com.tadeucruz.booking.service;

import static com.tadeucruz.booking.enums.BookingStatus.ACTIVATED;
import static com.tadeucruz.booking.enums.BookingStatus.CANCELED;
import static com.tadeucruz.booking.enums.ServiceLockTypes.RESERVATIONS;
import static java.time.temporal.ChronoUnit.DAYS;

import com.tadeucruz.booking.config.BookingConfig;
import com.tadeucruz.booking.exception.BookingConflictException;
import com.tadeucruz.booking.exception.BookingInvalidDates;
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
import java.util.stream.Stream;
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

    public Booking cancelBooking(Integer bookingId) {

        var booking = getBookingById(bookingId);

        booking.setStatus(CANCELED);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking createBooking(Integer roomId, Integer userId, LocalDateTime startDate,
        LocalDateTime endTime) {

        serviceLockRepository.findByName(RESERVATIONS.name());

        roomService.checkIfRoomExistsAndEnabled(roomId);
        // TODO: Need check for userService if user exist

        checkIfBookingDatesAreValid(roomId, startDate, endTime);

        var booking = Booking.builder()
            .roomId(roomId)
            .userId(userId)
            .startDate(startDate)
            .endDate(endTime)
            .status(ACTIVATED)
            .build();

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateBooking(Integer bookingId, LocalDateTime startDate,
        LocalDateTime endTime) {

        serviceLockRepository.findByName(RESERVATIONS.name());

        var booking = getBookingById(bookingId);

        checkIfBookingDatesAreValid(booking.getRoomId(), bookingId, startDate, endTime);

        booking.setStartDate(startDate);
        booking.setEndDate(endTime);

        return bookingRepository.save(booking);
    }

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

    private void checkIfBookingDatesAreValid(Integer roomId, LocalDateTime startDate,
        LocalDateTime endTime) {

        checkIfBookingDatesAreValid(roomId, null, startDate, endTime);
    }

    private void checkIfBookingDatesAreValid(Integer roomId, Integer bookingId,
        LocalDateTime startDate, LocalDateTime endTime) {

        checkIfStartDateIsAfterEndDate(startDate, endTime);
        checkIfStartDateIsValid(startDate);
        checkIfUserIsBookingDaysInRowIsMoreTheAllowedDays(startDate, endTime);
        checkIfUserIsBookingDaysInAdvanceIsMoreTheAllowedDays(startDate);
        getBookingStreamToCheckDateConflicts(roomId, bookingId, startDate, endTime);

    }

    private void checkIfStartDateIsAfterEndDate(LocalDateTime startDate, LocalDateTime endTime) {

        if (startDate.isAfter(endTime)) {

            throw new BookingInvalidDates(
                messageSourceService.getMessage("booking.startDate.is.after.endDate")
            );
        }

    }

    private void checkIfStartDateIsValid(LocalDateTime startDate) {

        var tomorrow = LocalDate.now().atStartOfDay().plusDays(1);

        if (startDate.isBefore(tomorrow)) {

            throw new BookingInvalidDates(
                messageSourceService.getMessage("booking.startDate.is.today")
            );
        }
    }

    private void checkIfUserIsBookingDaysInRowIsMoreTheAllowedDays(LocalDateTime startDate,
        LocalDateTime endTime) {

        var maxEndTime = startDate.plusDays(bookingConfig.getMaxDaysInRow()).plusDays(1)
            .minusSeconds(1);

        if (endTime.isAfter(maxEndTime)) {

            throw new BookingInvalidDates(
                messageSourceService.getMessage("booking.max.days.in.rows",
                    bookingConfig.getMaxDaysInRow())
            );
        }
    }

    private void checkIfUserIsBookingDaysInAdvanceIsMoreTheAllowedDays(LocalDateTime starDate) {

        var maxStartTime = LocalDate.now().atStartOfDay()
            .plusDays(bookingConfig.getMaxDaysAdvance());

        if (starDate.isAfter(maxStartTime)) {

            throw new BookingInvalidDates(
                messageSourceService.getMessage("booking.max.days.in.advance",
                    bookingConfig.getMaxDaysAdvance())
            );
        }
    }

    private void getBookingStreamToCheckDateConflicts(Integer roomId, Integer bookingId,
        LocalDateTime startDate, LocalDateTime endTime) {

        var bookingsBetweenStartDate = bookingRepository.findByRoomIdAndStatusAndStartDateBetween(
            roomId, ACTIVATED, startDate, endTime);
        var bookingsBetweenEndDate = bookingRepository.findByRoomIdAndStatusAndEndDateBetween(
            roomId, ACTIVATED, startDate, endTime);
        var bookingBetweenDates = bookingRepository.findByRoomIdAndStatusAndBetweenStartDateAndEndDate(
            roomId, ACTIVATED, startDate, endTime);

        var bookings = Stream.of(
                bookingsBetweenStartDate,
                bookingsBetweenEndDate,
                bookingBetweenDates)
            .flatMap(List::stream)
            .filter(booking -> !booking.getId().equals(bookingId))
            .toList();

        if (!bookings.isEmpty()) {

            throw new BookingConflictException(
                messageSourceService.getMessage("booking.date.conflict")
            );
        }
    }


}
