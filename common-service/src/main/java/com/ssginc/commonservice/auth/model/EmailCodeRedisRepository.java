package com.ssginc.commonservice.auth.model;

import org.springframework.data.repository.CrudRepository;

/**
 * @author Queue-ri
 */

public interface EmailCodeRedisRepository extends CrudRepository<RedisEmailValidationCode, String> {
}
