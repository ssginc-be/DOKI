package com.ssginc.commonservice.auth.model;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * @author Queue-ri
 */

public interface PhoneCodeRedisRepository extends CrudRepository<RedisPhoneValidationCode, String> {
}
