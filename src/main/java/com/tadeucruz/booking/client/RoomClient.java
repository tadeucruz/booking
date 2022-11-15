package com.tadeucruz.booking.client;

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

    // TODO: Move this to config
    private static final String ENDPOINT = "https://6372d30c348e947299fd79e5.mockapi.io/api/v1/room/";

    private final RestTemplate restTemplate;

    public Optional<RoomResponse> getRoomById(Integer roomId) {

        try {

            var response = restTemplate.getForEntity(ENDPOINT + roomId, RoomResponse.class);

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.error("Error calling getRoomById endpoint", e);

            return Optional.empty();
        }
    }

}
