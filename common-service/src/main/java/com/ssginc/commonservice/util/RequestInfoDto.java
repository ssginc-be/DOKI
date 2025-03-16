package com.ssginc.commonservice.util;

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
public class RequestInfoDto {

    private Long memberCode;

    private String memberRole;

    private String requestUuid;

}
