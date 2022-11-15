package com.tadeucruz.booking.client;

import com.tadeucruz.booking.config.BookingConfig;
import com.tadeucruz.booking.model.rest.RoomResponse;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor
@Slf4j
public class RoomClient {

    private final BookingConfig bookingConfig;

    private final RestTemplate restTemplate;

    public Optional<RoomResponse> getRoomById(Integer roomId) {

        try {

            var response = restTemplate.getForEntity(bookingConfig.getRoomEndpoint() + roomId,
                RoomResponse.class);

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.error("Error calling getRoomById endpoint", e);

            return Optional.empty();
        }
    }

}
