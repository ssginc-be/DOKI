package com.ssginc.commonservice.store.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ssginc.commonservice.reserve.model.ReservationEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * @author Queue-ri
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEntryResponseDto {
    /*
        특정 팝업스토어의 선택한 날짜에 대한 예약 엔트리
    */

    private Long reservationEntryId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime entryTime;

    private ReservationEntry.EntryStatus entryStatus;
}
