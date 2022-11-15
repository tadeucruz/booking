package com.tadeucruz.booking.model.rest;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationBookingRequest {

    private Integer userId;
    private LocalDate startDate;
    private LocalDate endDate;

}
