package com.ssginc.apigateway.feign.dto;

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
public class RequestMemberInfoDto {
    private Long memberCode; // 조회 최적화용 PK
    private String memberId; // 로그인용 ID
    private String memberName;
    private String memberRole;
}
