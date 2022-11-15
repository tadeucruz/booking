package com.tadeucruz.booking.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.tadeucruz.booking.exception.BookingConflictException;
import com.tadeucruz.booking.exception.BookingInvalidDates;
import com.tadeucruz.booking.exception.BookingNotFoundException;
import com.tadeucruz.booking.exception.RoomNotFoundException;
import com.tadeucruz.booking.model.rest.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ErrorResponse> handleBookingConflictException(Exception exception) {
        return buildErrorResponse(exception, BAD_REQUEST);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookingNotFoundException(Exception exception) {
        return buildErrorResponse(exception, BAD_REQUEST);
    }

    @ExceptionHandler(BookingInvalidDates.class)
    public ResponseEntity<ErrorResponse> handleBookingInvalidDates(Exception exception) {
        return buildErrorResponse(exception, BAD_REQUEST);
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoomNotFoundException(Exception exception) {
        return buildErrorResponse(exception, BAD_REQUEST);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception exception,
        HttpStatus httpStatus) {

        log.error("Error", exception);

        var errorResponse = ErrorResponse.builder()
            .status(httpStatus.value())
            .message(exception.getMessage())
            .build();

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

}
