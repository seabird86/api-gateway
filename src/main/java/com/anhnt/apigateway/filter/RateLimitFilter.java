package com.anhnt.apigateway.filter;

import com.anhnt.apigateway.constant.HeaderConstant;
import com.anhnt.common.domain.exception.InvalidRequestException;
import com.anhnt.common.domain.response.ErrorFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
@AllArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private ClientRateLimiter rateLimiter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        HttpHeaders headers = exchange.getRequest().getHeaders();
//        if (headers.getFirst(HeaderConstant.CLIENT_ID).equalsIgnoreCase("ANH")){
//            return chain.filter(exchange);
//        }
        return  rateLimiter.isAllowed(exchange).flatMap( res -> {
            res.getHeaders().forEach((k,v)->exchange.getResponse().getHeaders().add(k, v));
            if (res.isAllowed()) {
                return chain.filter(exchange);
            }
            String lang = exchange.getLocaleContext().getLocale().getLanguage();
            return Mono.error(new InvalidRequestException(ErrorFactory.ApiGatewayError.TOO_MANY_REQUESTS.apply(lang, null)));
        });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}