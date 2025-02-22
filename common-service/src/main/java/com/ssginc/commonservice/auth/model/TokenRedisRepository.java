package com.ssginc.commonservice.auth.model;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * @author Queue-ri
 */

public interface TokenRedisRepository extends CrudRepository<RedisRefreshToken, Long> {
    Optional<RedisRefreshToken> findByRefreshToken(String refreshToken);
}
