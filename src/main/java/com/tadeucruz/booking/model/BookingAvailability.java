package com.tadeucruz.booking.model;

import com.tadeucruz.booking.enums.BookingAvailabilityStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingAvailability {

    private LocalDate day;
    private BookingAvailabilityStatus status;

}
