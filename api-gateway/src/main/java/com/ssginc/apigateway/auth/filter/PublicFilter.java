package com.ssginc.apigateway.auth.filter;

import com.ssginc.apigateway.auth.exception.CustomException;
import com.ssginc.apigateway.auth.exception.ErrorCode;
import com.ssginc.apigateway.feign.AuthFeignClient;
import com.ssginc.apigateway.feign.dto.RequestMemberInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Queue-ri
 */

@Slf4j
@Component
public class PublicFilter extends AbstractGatewayFilterFactory<PublicFilter.Config> {

    //private final AuthFeignClient authFeignClient;

    public PublicFilter(/*AuthFeignClient authFeignClient*/) {
        super(PublicFilter.Config.class);
        //this.authFeignClient = authFeignClient;
    }

    public static class Config {

    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
//            // 현재 요청의 request info 가져오기
//            ServerHttpRequest request = exchange.getRequest();
//            List<HttpCookie> accessTokenCookie = request.getCookies().get("accessToken"); // null or not
//
//            // 1. accessToken이 존재할 경우, JWT 유효성 검사 및 정보 가져오기
//            if (accessTokenCookie != null) {
//                String accessToken = accessTokenCookie.get(0).getValue();
//                ResponseEntity<RequestMemberInfoDto> response = authFeignClient.validateAndParse(accessToken);
//
//                // 200 OK -> 정보 가져오기
//                if (response.getStatusCode() == HttpStatus.OK) {
//                    log.error("인증 성공");
//                    RequestMemberInfoDto dto = response.getBody();
//
//                    exchange.getRequest()
//                            .mutate()
//                            .header("x-gateway-member-code", dto.getMemberCode().toString())
//                            .build();
//                } else {
//                    // 1-1. accessToken을 검증했으나 서비스 통신 단에서 fail
//                    log.error("인증 실패: {}", response.getStatusCode().value());
//                    throw new CustomException(ErrorCode.INVALID_TOKEN);
//                }
//            }

            // 다음 필터로 체인 전달
            return chain.filter(exchange);
        });
    }

}
