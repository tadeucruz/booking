package com.tadeucruz.booking.model.rest;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationBookingRequest {

    private String userId;
    private LocalDate startDate;
    private LocalDate endDate;

}
