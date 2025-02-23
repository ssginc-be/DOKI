package com.ssginc.commonservice.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

/**
 * @author Queue-ri
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequestDto {
    @JsonProperty("member_id")
    private String memberId;

    @JsonProperty("member_pw")
    private String memberPw;

    @JsonProperty("member_name")
    private String memberName;

    @JsonProperty("member_phone")
    private String memberPhone;

    @JsonProperty("member_birth")
    private LocalDate memberBirth;

    @JsonProperty("member_gender")
    private String memberGender;
}
