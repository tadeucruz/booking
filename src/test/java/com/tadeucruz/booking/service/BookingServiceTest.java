package com.tadeucruz.booking.service;

import static com.tadeucruz.booking.enums.BookingStatus.ACTIVATED;
import static com.tadeucruz.booking.enums.BookingStatus.CANCELED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tadeucruz.booking.config.BookingConfig;
import com.tadeucruz.booking.exception.BookingNotFoundException;
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

    private static final LocalDateTime startDate = LocalDate.now().atStartOfDay();
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

    private Booking buildBooking() {

        return Booking.builder()
            .id(1)
            .roomId(1)
            .userId(100)
            .status(ACTIVATED)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }
}