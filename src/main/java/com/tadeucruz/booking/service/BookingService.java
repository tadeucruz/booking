package com.tadeucruz.booking.service;

import static com.tadeucruz.booking.enums.BookingAvailabilityStatus.BOOKED;
import static com.tadeucruz.booking.enums.BookingAvailabilityStatus.FREE;
import static com.tadeucruz.booking.enums.BookingStatus.ACTIVATED;
import static com.tadeucruz.booking.enums.BookingStatus.CANCELED;
import static com.tadeucruz.booking.enums.ServiceLockTypes.RESERVATIONS;
import static java.time.temporal.ChronoUnit.DAYS;

import com.tadeucruz.booking.config.BookingConfig;
import com.tadeucruz.booking.exception.BookingConflictException;
import com.tadeucruz.booking.exception.BookingInvalidDates;
import com.tadeucruz.booking.exception.BookingNotFoundException;
import com.tadeucruz.booking.model.BookingAvailability;
import com.tadeucruz.booking.model.db.Booking;
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

    public List<BookingAvailability> getBookingAvailability(Integer roomId) {

        roomService.checkIfRoomExistsAndEnabled(roomId);

        var result = new ArrayList<BookingAvailability>();

        var today = LocalDate.now().atStartOfDay();

        var setDaysAlreadyBooked = new HashSet<LocalDate>();

        var bookings = bookingRepository.findByRoomIdAndStatusAndStartDateAfter(roomId, ACTIVATED,
            today);

        for (Booking booking : bookings) {
            LocalDate controlDate = booking.getStartDate().toLocalDate();
            var daysBetween = DAYS.between(booking.getStartDate(), booking.getEndDate());

            for (int i = 0; i <= daysBetween; i++) {
                setDaysAlreadyBooked.add(controlDate);
                controlDate = controlDate.plusDays(1);
            }

        }

        LocalDate controlDate = today.toLocalDate().plusDays(1);
        for (int i = 1; i < bookingConfig.getMaxDaysAdvance(); i++) {

            var status = setDaysAlreadyBooked.contains(controlDate) ? BOOKED : FREE;

            var bookingAvailability = BookingAvailability.builder()
                .day(controlDate)
                .status(status)
                .build();

            controlDate = controlDate.plusDays(1);

            result.add(bookingAvailability);
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
        checkIfExistBookingDateConflict(roomId, bookingId, startDate, endTime);

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

    private void checkIfExistBookingDateConflict(Integer roomId, Integer bookingId,
        LocalDateTime startDate, LocalDateTime endDate) {

        var bookingsBetweenStartDate = bookingRepository.findByRoomIdAndStatusAndStartDateBetween(
            roomId, ACTIVATED, startDate, endDate);
        var bookingsBetweenEndDate = bookingRepository.findByRoomIdAndStatusAndEndDateBetween(
            roomId, ACTIVATED, startDate, endDate);
        var bookingBetweenDates = bookingRepository.findByRoomIdAndStatusAndBetweenStartDateAndEndDate(
            roomId, ACTIVATED, startDate, endDate);

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
