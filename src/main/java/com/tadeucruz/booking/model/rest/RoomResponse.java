package com.tadeucruz.booking.model.rest;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RoomResponse {

    private String id;
    private String hotelId;
    private String roomNumber;
    private String status;
    private LocalDateTime createdAt;

}
