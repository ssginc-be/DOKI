package com.ssginc.apigateway.auth.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Queue-ri
 */

@Slf4j
@Component
public class GlobalUuidFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUuid = generateUuid();
        exchange.getRequest().mutate().header("x-gateway-request-uuid", requestUuid);

        log.info("generated request uuid: {}", requestUuid); // logging

        return chain.filter(exchange);
    }

    private String generateUuid() {
        // request UUID 발급
        return UUID.randomUUID().toString();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
