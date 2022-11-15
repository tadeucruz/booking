package com.tadeucruz.booking.controller;

import com.tadeucruz.booking.model.rest.BookingAvailabilityResponse;
import com.tadeucruz.booking.model.rest.BookingResponse;
import com.tadeucruz.booking.model.rest.CreateBookingRequest;
import com.tadeucruz.booking.model.rest.UpdateBookingRequest;
import com.tadeucruz.booking.service.BookingService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/booking")
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllReservation() {

        var bookings = bookingService.getAllBooking();

        var bookingResponses = bookings.stream()
            .map(booking -> modelMapper.map(booking, BookingResponse.class)).toList();

        return ResponseEntity.ok(bookingResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Integer id) {

        var booking = bookingService.getBookingById(id);

        var bookingResponse = modelMapper.map(booking, BookingResponse.class);

        return ResponseEntity.ok(bookingResponse);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
        @RequestBody CreateBookingRequest request) {

        var booking = bookingService.createBooking(
            request.getRoomId(),
            request.getUserId(),
            request.getStartDate().atStartOfDay(),
            request.getEndDate().atStartOfDay().plusDays(1).minusSeconds(1)
        );

        var bookingResponse = modelMapper.map(booking, BookingResponse.class);

        return ResponseEntity.ok(bookingResponse);
    }

    @PostMapping("{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Integer bookingId) {

        var booking = bookingService.cancelBooking(bookingId);

        var bookingResponse = modelMapper.map(booking, BookingResponse.class);

        return ResponseEntity.ok(bookingResponse);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Integer bookingId,
        @RequestBody
        UpdateBookingRequest request) {

        var booking = bookingService.updateBooking(
            bookingId,
            request.getStartDate().atStartOfDay(),
            request.getEndDate().atStartOfDay().plusDays(1).minusSeconds(1)
        );

        var bookingResponse = modelMapper.map(booking, BookingResponse.class);

        return ResponseEntity.ok(bookingResponse);
    }

    @GetMapping("/availability/{roomId}")
    public ResponseEntity<List<BookingAvailabilityResponse>> getAllAvailability(
        @PathVariable Integer roomId) {

        var list = bookingService.getFreeTimes(roomId);

        return ResponseEntity.ok(list);
    }

}
