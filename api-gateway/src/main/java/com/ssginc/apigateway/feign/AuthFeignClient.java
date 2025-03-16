package com.ssginc.apigateway.feign;

import com.ssginc.apigateway.feign.dto.RequestMemberInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Queue-ri
 */

@FeignClient(name="common-service", path="/v1/auth")
public interface AuthFeignClient {

    @GetMapping("/info")
    ResponseEntity<RequestMemberInfoDto> validateAndParse(@RequestParam("token") String accessToken);
}
