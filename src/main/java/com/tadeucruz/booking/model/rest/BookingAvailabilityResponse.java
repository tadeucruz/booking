package com.tadeucruz.booking.model.rest;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingAvailabilityResponse {

    private LocalDate day;
    private String status;

}
