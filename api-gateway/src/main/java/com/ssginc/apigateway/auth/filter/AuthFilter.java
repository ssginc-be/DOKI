package com.ssginc.apigateway.auth.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Queue-ri
 */

@Slf4j(topic = "AuthFilter")
@Component
public class AuthFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 현재 요청의 request info 가져오기
        ServerHttpRequest request = exchange.getRequest();

        request.mutate()
                .headers(header -> {
                    // JWT 유효성 검사 및 정보 가져오기
                    

                    // request header에 member code 추가
                    header.add("x-gateway-member-code", 1);

                    // request header에 member role 추가
                    header.add("x-gateway-member-role", 1);
                })
                .build();

        // 다음 필터로 체인 전달
        return chain.filter(exchange);
    }
}
