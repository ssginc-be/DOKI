package com.ssginc.reserveservice.reserve.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime reservedDateTime;

    private Integer headcount;
}
