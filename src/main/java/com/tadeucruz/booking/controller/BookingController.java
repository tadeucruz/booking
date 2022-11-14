package com.tadeucruz.booking.controller;

import com.tadeucruz.booking.model.rest.BookingResponse;
import com.tadeucruz.booking.service.BookingService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/booking")
@AllArgsConstructor
public class BookingController {

    private BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllReservation() {

        var modelMapper = new ModelMapper();

        var bookings = bookingService.getAllBooking();

        var bookingResponses = bookings.stream()
            .map(booking -> modelMapper.map(booking, BookingResponse.class)).toList();

        return ResponseEntity.ok(bookingResponses);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createReservation() {

        var modelMapper = new ModelMapper();

        var now = LocalDateTime.now().minusDays(5);

        var booking = bookingService.createBooking(UUID.randomUUID(), UUID.randomUUID(), now,
            now.plusDays(6));

        var bookingResponse = modelMapper.map(booking, BookingResponse.class);

        return ResponseEntity.ok(bookingResponse);
    }

}
