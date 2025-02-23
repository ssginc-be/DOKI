package com.ssginc.commonservice.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class SignInRequestDto {
    @JsonProperty("member_id")
    private String memberId;

    @JsonProperty("member_pw")
    private String memberPw;
}
