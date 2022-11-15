package com.tadeucruz.booking.model.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateBookingRequest {

    @Schema(type = "string", example = "1")
    @NotBlank
    private Integer roomId;

    @Schema(type = "string", example = "44")
    @NotBlank
    private Integer userId;

    @Schema(type = "date", example = "2022-11-15")
    @NotBlank
    @Future
    private LocalDate startDate;

    @Schema(type = "date", example = "2022-11-17")
    @NotBlank
    @Future
    private LocalDate endDate;

}
