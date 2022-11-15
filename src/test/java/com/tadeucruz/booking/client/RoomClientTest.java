package com.tadeucruz.booking.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.tadeucruz.booking.config.BookingConfig;
import com.tadeucruz.booking.exception.RoomNotFoundException;
import com.tadeucruz.booking.model.rest.RoomResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RoomClientTest {

    private static final String ENDPOINT = "https://localhost/";

    private static final LocalDateTime DATE = LocalDateTime.now();

    @InjectMocks
    private RoomClient roomClient;

    @Mock
    private BookingConfig bookingConfig;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<RoomResponse> responseEntity;

    @BeforeEach
    void setUp() {
        when(bookingConfig.getRoomEndpoint()).thenReturn(ENDPOINT);
    }

    @Test
    void testGetRoomByIdWithError() {

        var roomId = 1;

        when(restTemplate.getForEntity(ENDPOINT + roomId, RoomResponse.class)).thenThrow(
            RoomNotFoundException.class);

        var result = roomClient.getRoomById(roomId);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRoomByIdWithSucess() {

        var roomId = 1;
        var expectedRoomResponse = buildRoomResponse();

        when(restTemplate.getForEntity(ENDPOINT + roomId, RoomResponse.class)).thenReturn(
            responseEntity);
        when(responseEntity.getBody()).thenReturn(buildRoomResponse());

        var result = roomClient.getRoomById(roomId);

        assertTrue(result.isPresent());
        assertEquals(expectedRoomResponse, result.get());
    }

    private RoomResponse buildRoomResponse() {
        return RoomResponse.builder()
            .id("1")
            .hotelId("1")
            .roomNumber("101")
            .createdAt(DATE)
            .status("STATUS")
            .build();
    }

}