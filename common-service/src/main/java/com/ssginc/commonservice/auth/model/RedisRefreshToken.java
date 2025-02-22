package com.ssginc.commonservice.auth.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

/**
 * @author Queue-ri
 */

@Builder
@Getter
@RedisHash(value = "refreshToken", timeToLive = 21600L) // 6hr (ex. 180L == 180s for testing)
public class RedisRefreshToken {
    @Id
    private Long memberCode;

    @Indexed
    private String refreshToken;

    @TimeToLive
    private Long expiration;
}
