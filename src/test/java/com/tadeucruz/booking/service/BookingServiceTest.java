package com.tadeucruz.booking.service;

import static com.tadeucruz.booking.enums.BookingAvailabilityStatus.BOOKED;
import static com.tadeucruz.booking.enums.BookingAvailabilityStatus.FREE;
import static com.tadeucruz.booking.enums.BookingStatus.ACTIVATED;
import static com.tadeucruz.booking.enums.BookingStatus.CANCELED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final LocalDateTime startDate = LocalDate.now().plusDays(1).atStartOfDay();
    private static final LocalDateTime endDate = startDate.plusDays(1).minusSeconds(1);

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ServiceLockRepository serviceLockRepository;

    @Mock
    private RoomService roomService;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private BookingConfig bookingConfig;

    @Test
    void test_getAllBooking() {

        var expected = List.of(
            buildBooking(),
            buildBooking()
        );

        when(bookingRepository.findAll()).thenReturn(expected);

        var result = bookingService.getAllBooking();

        assertEquals(expected, result);
    }

    @Test
    void test_getBookingById_withInvalidId() {

        var bookId = 1;
        var errorMessage = "Error";

        when(bookingRepository.findById(bookId)).thenReturn(Optional.empty());
        when(messageSourceService.getMessage("booking.invalid.id", bookId))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingNotFoundException.class,
            () -> bookingService.getBookingById(bookId),
            errorMessage
        );
    }

    @Test
    void test_getBookingById_withValidId() {

        var bookId = 1;
        var booking = buildBooking();
        var expectedBooking = buildBooking();

        when(bookingRepository.findById(bookId)).thenReturn(Optional.of(booking));

        var result = bookingService.getBookingById(bookId);

        assertEquals(expectedBooking, result);
    }

    @Test
    void test_getOptionalBookingById() {

        var bookId = 1;
        var booking = buildBooking();
        var expectedBooking = buildBooking();

        when(bookingRepository.findById(bookId)).thenReturn(Optional.of(booking));

        var result = bookingService.getOptionalBookingById(bookId);

        assertTrue(result.isPresent());
        assertEquals(expectedBooking, result.get());
    }

    @Test
    void test_cancelBooking() {

        var bookId = 1;
        var booking = buildBooking();

        var expectedBooking = buildBooking();
        expectedBooking.setStatus(CANCELED);

        when(bookingRepository.findById(bookId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        var result = bookingService.cancelBooking(bookId);

        assertEquals(expectedBooking, result);
        verify(bookingRepository).save(booking);
    }

    @Test
    void test_createBooking_ruleStartDateIsAfterEndDate() {

        var roomId = 1;
        var userId = 1;
        var startDate = LocalDate.now().atStartOfDay();
        var endDate = startDate.plusDays(1).minusSeconds(1);
        var errorMessage = "Error";

        when(messageSourceService.getMessage("booking.startDate.is.after.endDate"))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingInvalidDates.class,
            () -> bookingService.createBooking(roomId, userId, endDate, startDate),
            errorMessage
        );

    }

    @Test
    void test_createBooking_ruleStartDateIsValid() {

        var roomId = 1;
        var userId = 1;
        var startDate = LocalDate.now().atStartOfDay();
        var endDate = startDate.plusDays(1).minusSeconds(1);
        var errorMessage = "Error";

        when(messageSourceService.getMessage("booking.startDate.is.today"))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingInvalidDates.class,
            () -> bookingService.createBooking(roomId, userId, startDate, endDate),
            errorMessage
        );

    }

    //

    @Test
    void test_createBooking_ruleUserIsBookingDaysInRowIsMoreTheAllowedDays() {

        var roomId = 1;
        var userId = 1;
        var startDate = LocalDate.now().plusDays(1).atStartOfDay();
        var endDate = startDate.plusDays(4).minusSeconds(1);
        var errorMessage = "Error";
        var maxDayInRow = 2;

        when(bookingConfig.getMaxDaysInRow()).thenReturn(maxDayInRow);
        when(messageSourceService.getMessage("booking.max.days.in.rows", maxDayInRow))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingInvalidDates.class,
            () -> bookingService.createBooking(roomId, userId, startDate, endDate),
            errorMessage
        );

    }

    @Test
    void test_createBooking_ruleUserIsBookingDaysInAdvanceIsMoreTheAllowedDays() {

        var roomId = 1;
        var userId = 1;
        var startDate = LocalDate.now().plusDays(30).atStartOfDay();
        var endDate = startDate.plusDays(1).minusSeconds(1);
        var errorMessage = "Error";
        var maxDaysAdvance = 2;

        when(bookingConfig.getMaxDaysAdvance()).thenReturn(maxDaysAdvance);
        when(messageSourceService.getMessage("booking.max.days.in.advance", maxDaysAdvance))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingInvalidDates.class,
            () -> bookingService.createBooking(roomId, userId, startDate, endDate),
            errorMessage
        );

    }

    @Test
    void test_createBooking_ruleExistBookingDateConflict() {

        var roomId = 1;
        var userId = 1;
        var startDate = LocalDate.now().plusDays(30).atStartOfDay();
        var endDate = startDate.plusDays(1).minusSeconds(1);
        var errorMessage = "Error";
        var maxDaysAdvance = 30;
        var maxDayInRow = 3;

        when(bookingConfig.getMaxDaysAdvance()).thenReturn(maxDaysAdvance);
        when(bookingConfig.getMaxDaysInRow()).thenReturn(maxDayInRow);

        when(bookingRepository.findByRoomIdAndStatusAndStartDateBetween(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of(buildBooking()));
        when(bookingRepository.findByRoomIdAndStatusAndEndDateBetween(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of(buildBooking()));
        when(bookingRepository.findByRoomIdAndStatusAndBetweenStartDateAndEndDate(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of(buildBooking()));

        when(messageSourceService.getMessage("booking.date.conflict"))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingConflictException.class,
            () -> bookingService.createBooking(roomId, userId, startDate, endDate),
            errorMessage
        );

    }

    @Test
    void test_createBooking_Success() {

        var roomId = 1;
        var userId = 1;
        var maxDaysAdvance = 30;
        var maxDayInRow = 3;

        var expectedBooking = buildBooking();

        when(bookingConfig.getMaxDaysAdvance()).thenReturn(maxDaysAdvance);
        when(bookingConfig.getMaxDaysInRow()).thenReturn(maxDayInRow);

        when(bookingRepository.findByRoomIdAndStatusAndStartDateBetween(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of());
        when(bookingRepository.findByRoomIdAndStatusAndEndDateBetween(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of());
        when(bookingRepository.findByRoomIdAndStatusAndBetweenStartDateAndEndDate(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of());

        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            var tmpBooking = (Booking) i.getArguments()[0];
            tmpBooking.setId(1);
            return tmpBooking;
        });

        var result = bookingService.createBooking(roomId, userId, startDate, endDate);

        assertEquals(expectedBooking, result);
    }

    @Test
    void test_updateBooking_ruleStartDateIsAfterEndDate() {

        var bookingId = 1;
        var booking = buildBooking();
        var startDate = LocalDate.now().atStartOfDay();
        var endDate = startDate.plusDays(1).minusSeconds(1);
        var errorMessage = "Error";

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(messageSourceService.getMessage("booking.startDate.is.after.endDate"))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingInvalidDates.class,
            () -> bookingService.updateBooking(bookingId, endDate, startDate),
            errorMessage
        );

    }

    @Test
    void test_updateBooking_ruleStartDateIsValid() {

        var bookingId = 1;
        var booking = buildBooking();
        var startDate = LocalDate.now().atStartOfDay();
        var endDate = startDate.plusDays(1).minusSeconds(1);
        var errorMessage = "Error";

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(messageSourceService.getMessage("booking.startDate.is.today"))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingInvalidDates.class,
            () -> bookingService.updateBooking(bookingId, startDate, endDate),
            errorMessage
        );

    }

    @Test
    void test_updateBooking_ruleUserIsBookingDaysInRowIsMoreTheAllowedDays() {

        var bookingId = 1;
        var booking = buildBooking();
        var startDate = LocalDate.now().plusDays(1).atStartOfDay();
        var endDate = startDate.plusDays(4).minusSeconds(1);
        var errorMessage = "Error";
        var maxDayInRow = 2;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingConfig.getMaxDaysInRow()).thenReturn(maxDayInRow);
        when(messageSourceService.getMessage("booking.max.days.in.rows", maxDayInRow))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingInvalidDates.class,
            () -> bookingService.updateBooking(bookingId, startDate, endDate),
            errorMessage
        );

    }

    @Test
    void test_updateBooking_ruleUserIsBookingDaysInAdvanceIsMoreTheAllowedDays() {

        var bookingId = 1;
        var booking = buildBooking();
        var startDate = LocalDate.now().plusDays(30).atStartOfDay();
        var endDate = startDate.plusDays(1).minusSeconds(1);
        var errorMessage = "Error";
        var maxDaysAdvance = 2;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingConfig.getMaxDaysAdvance()).thenReturn(maxDaysAdvance);
        when(messageSourceService.getMessage("booking.max.days.in.advance", maxDaysAdvance))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingInvalidDates.class,
            () -> bookingService.updateBooking(bookingId, startDate, endDate),
            errorMessage
        );

    }

    @Test
    void test_updateBooking_ruleExistBookingDateConflict() {

        var roomId = 1;
        var bookingId = 1;
        var booking = buildBooking();
        var startDate = LocalDate.now().plusDays(30).atStartOfDay();
        var endDate = startDate.plusDays(1).minusSeconds(1);
        var errorMessage = "Error";
        var maxDaysAdvance = 30;
        var maxDayInRow = 3;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingConfig.getMaxDaysAdvance()).thenReturn(maxDaysAdvance);
        when(bookingConfig.getMaxDaysInRow()).thenReturn(maxDayInRow);

        when(bookingRepository.findByRoomIdAndStatusAndStartDateBetween(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of(buildBookingWithId2()));
        when(bookingRepository.findByRoomIdAndStatusAndEndDateBetween(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of(buildBookingWithId2()));
        when(bookingRepository.findByRoomIdAndStatusAndBetweenStartDateAndEndDate(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of(buildBookingWithId2()));

        when(messageSourceService.getMessage("booking.date.conflict"))
            .thenReturn(errorMessage);

        assertThrowsExactly(
            BookingConflictException.class,
            () -> bookingService.updateBooking(bookingId, startDate, endDate),
            errorMessage
        );

    }

    @Test
    void test_updateBooking_success() {

        var roomId = 1;
        var bookingId = 1;
        var maxDaysAdvance = 30;
        var maxDayInRow = 3;

        var expectedBooking = buildBooking();

        var booking = buildBooking();
        booking.setStartDate(LocalDateTime.MAX);
        booking.setEndDate(LocalDateTime.MAX);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingConfig.getMaxDaysAdvance()).thenReturn(maxDaysAdvance);
        when(bookingConfig.getMaxDaysInRow()).thenReturn(maxDayInRow);

        when(bookingRepository.findByRoomIdAndStatusAndStartDateBetween(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of(buildBooking()));
        when(bookingRepository.findByRoomIdAndStatusAndEndDateBetween(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of(buildBooking()));
        when(bookingRepository.findByRoomIdAndStatusAndBetweenStartDateAndEndDate(
            roomId, ACTIVATED, startDate, endDate)).thenReturn(List.of(buildBooking()));

        when(bookingRepository.save(booking)).thenReturn(booking);

        var result = bookingService.updateBooking(roomId, startDate, endDate);

        assertEquals(expectedBooking, result);

    }

    @Test
    void test_getBookingAvailability() {

        var roomId = 1;
        var maxDaysAdvance = 3;

        var controlDate = LocalDate.now().plusDays(1);
        var expected = List.of(
            BookingAvailability.builder()
                .day(controlDate)
                .status(BOOKED)
                .build(),
            BookingAvailability.builder()
                .day(controlDate.plusDays(1))
                .status(FREE)
                .build()
        );

        when(bookingRepository.findByRoomIdAndStatusAndStartDateAfter(roomId, ACTIVATED,
            LocalDate.now().atStartOfDay())).thenReturn(List.of(buildBooking()));
        when(bookingConfig.getMaxDaysAdvance()).thenReturn(maxDaysAdvance);

        var result = bookingService.getBookingAvailability(roomId);

        assertEquals(expected, result);
    }

    private Booking buildBooking() {

        return Booking.builder()
            .id(1)
            .roomId(1)
            .userId(1)
            .status(ACTIVATED)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }

    private Booking buildBookingWithId2() {

        var booking = buildBooking();
        booking.setId(2);

        return booking;
    }
}