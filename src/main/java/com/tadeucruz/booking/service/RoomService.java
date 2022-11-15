package com.tadeucruz.booking.service;

import static com.tadeucruz.booking.enums.RoomStatus.ENABLED;

import com.tadeucruz.booking.client.RoomClient;
import com.tadeucruz.booking.exception.RoomNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoomService {

    private final RoomClient roomClient;
    private final MessageSourceService messageSourceService;

    public void checkIfRoomExistsAndEnabled(Integer roomId) {

        var optionalRoomResponse = roomClient.getRoomById(roomId);

        if (optionalRoomResponse.isEmpty()) {
            throw new RoomNotFoundException();
        }

        var roomResponse = optionalRoomResponse.get();

        if (!ENABLED.name().equals(roomResponse.getStatus())) {
            throw new RoomNotFoundException(
                messageSourceService.getMessage("booking.room.invalid.id", roomId)
            );
        }
    }
}
