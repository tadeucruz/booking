package com.tadeucruz.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class BookingConfig {

    @Value("${booking.max.days.in.row}")
    private Integer maxDaysInRow;

    @Value("${booking.max.days.in.advance}")
    private Integer maxDaysAdvance;

    @Value("${booking.room.endpoint}")
    private String roomEndpoint;

}
