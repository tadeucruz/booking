package com.tadeucruz.booking.model;

import java.time.LocalDate;

import com.tadeucruz.booking.enums.BookingAvailabilityStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingAvailability {

    private LocalDate day;
    private BookingAvailabilityStatus status;

}
