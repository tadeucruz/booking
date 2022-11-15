package com.tadeucruz.booking.service;

import static com.tadeucruz.booking.enums.RoomStatus.DISABLED;
import static com.tadeucruz.booking.enums.RoomStatus.ENABLED;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.when;

import com.tadeucruz.booking.client.RoomClient;
import com.tadeucruz.booking.exception.RoomNotFoundException;
import com.tadeucruz.booking.model.rest.RoomResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomClient roomClient;

    @Mock
    private MessageSourceService messageSourceService;

    @Test
    void test_checkIfRoomExistsAndEnabled_invalidRoomId() {

        var roomId = 3;
        var error = "error";

        when(roomClient.getRoomById(roomId)).thenReturn(Optional.empty());
        when(messageSourceService.getMessage("booking.room.invalid.id", roomId))
            .thenReturn(error);

        assertThrowsExactly(
            RoomNotFoundException.class,
            () -> roomService.checkIfRoomExistsAndEnabled(roomId),
            error
        );
    }

    @Test
    void test_checkIfRoomExistsAndEnabled_disabledRoom() {

        var roomId = 3;
        var error = "error";

        var disableRoomResponse = buildDisableRoomResponse();

        when(roomClient.getRoomById(roomId)).thenReturn(Optional.of(disableRoomResponse));
        when(messageSourceService.getMessage("booking.room.invalid.id", roomId))
            .thenReturn(error);

        assertThrowsExactly(
            RoomNotFoundException.class,
            () -> roomService.checkIfRoomExistsAndEnabled(roomId),
            error
        );
    }

    @Test
    void test_checkIfRoomExistsAndEnabled_success() {

        var roomId = 1;

        when(roomClient.getRoomById(roomId)).thenReturn(Optional.of(buildEnableRoomResponse()));

        roomService.checkIfRoomExistsAndEnabled(roomId);
    }

    private RoomResponse buildEnableRoomResponse() {

        return RoomResponse.builder()
            .id("1")
            .hotelId("1")
            .roomNumber("201")
            .status(ENABLED.name())
            .createdAt(LocalDateTime.now())
            .build();
    }

    private RoomResponse buildDisableRoomResponse() {

        return RoomResponse.builder()
            .id("1")
            .hotelId("1")
            .roomNumber("201")
            .status(DISABLED.name())
            .createdAt(LocalDateTime.now())
            .build();
    }
}