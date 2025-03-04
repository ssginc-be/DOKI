package com.ssginc.reserveservice.reserve.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Queue-ri
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveRequestDto {
    private Long entryId;
    private Long memberCode;
    private Long storeId;
    private LocalDateTime reservedDateTime;
    private Integer headcount;
}
