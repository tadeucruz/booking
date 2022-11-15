package com.tadeucruz.booking.model.rest;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class RoomResponse {

    private String id;
    private String hotelId;
    private String roomNumber;
    private String status;
    private LocalDateTime createdAt;

}
