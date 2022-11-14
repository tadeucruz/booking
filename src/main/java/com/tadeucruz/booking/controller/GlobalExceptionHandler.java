package com.tadeucruz.booking.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.tadeucruz.booking.exception.BookingConflictException;
import com.tadeucruz.booking.model.rest.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<Object> handleBookingConflictException(Exception exception) {

        log.error("BookingConflictException", exception);

        var errorResponse = ErrorResponse.builder()
            .status(400)
            .message(exception.getMessage())
            .build();

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }


}
