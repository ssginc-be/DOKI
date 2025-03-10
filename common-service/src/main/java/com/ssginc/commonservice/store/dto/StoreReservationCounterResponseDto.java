package com.ssginc.commonservice.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Queue-ri
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreReservationCounterResponseDto {
    private Long confirmed;
    private Long refused;
    private Long canceled;
}
