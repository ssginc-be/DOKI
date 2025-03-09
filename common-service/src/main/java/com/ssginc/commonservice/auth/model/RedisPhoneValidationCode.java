package com.ssginc.commonservice.auth.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * @author Queue-ri
 */

@Builder
@Getter
@RedisHash(value = "phoneValidationCode", timeToLive = 300L) // 5min (ex. 180L == 180s)
public class RedisPhoneValidationCode {
    @Id
    private String validationCode;

    private String phoneNum;

    @TimeToLive
    private Long expiration;
}
