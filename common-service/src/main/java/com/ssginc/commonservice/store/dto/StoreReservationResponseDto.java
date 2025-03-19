package com.ssginc.commonservice.store.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * @author Queue-ri
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreReservationResponseDto {
    private Long reservationId;

    private LocalDate reservedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime reservedTime;

    private String memberName;

    private String memberPhone;

    private String reservationStatus;
}
