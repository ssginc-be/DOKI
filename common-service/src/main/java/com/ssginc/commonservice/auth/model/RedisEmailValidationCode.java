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
@RedisHash(value = "phoneValidationCode", timeToLive = 1800L) // 30min (ex. 180L == 180s)
public class RedisEmailValidationCode {
    @Id
    private String validationCode;

    private String email;

    @TimeToLive
    private Long expiration;
}
