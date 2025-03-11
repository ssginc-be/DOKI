package com.ssginc.commonservice.store.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ssginc.commonservice.reserve.model.ReservationEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author Queue-ri
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationLogResponseDto {
    /*
        특정 팝업스토어의 선택한 예약에 대한 예약상태 변경 로그
    */
    private Long reservationLogId;

    private Long reservationId;

    private String reserveMethodCode;

    private String reserveMethod;

    private String reservationStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
