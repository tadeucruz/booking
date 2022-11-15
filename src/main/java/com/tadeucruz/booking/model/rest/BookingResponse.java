package com.tadeucruz.booking.model.rest;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    private String id;
    private String status;
    private String roomId;
    private String userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
