package com.anhnt.apigateway.filter;

import com.anhnt.common.domain.response.ErrorFactory.ApiGatewayError;
import com.anhnt.common.domain.exception.InvalidRequestException;
import com.anhnt.common.utils.RSAUtil;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class SignatureFilter implements GlobalFilter, Ordered {

    public static final String SIGNATURE = "Signature";
    public static final String CLIENT_ID = "Client-ID";
    public static final String TIMESTAMP = "timestamp";
    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCO8gCZIZ7QXlgowYlCmmAVor2mjHMcTK9mv6DxJaRs/6ttakhmLJmHKdR66JQsgYfqyVUbpvs1ij1iHQABPWJB9M8HIl7v1lMMCp5ziUZl9+5N/ocXe/xIZzWpoj5GMgJpvVrGJ7KZ9rXFgJCRIQKXNj3ZjQGvIprXXsb424I7wIDAQAB\n-----END PUBLIC KEY-----";


    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String lang = exchange.getLocaleContext().getLocale().getLanguage();
        HttpHeaders headers = request.getHeaders();
        if (headers.getFirst(CLIENT_ID)==null){
            throw new InvalidRequestException(ApiGatewayError.HEADER_REQUIRED.apply(lang, List.of(CLIENT_ID)));
        }
        if (headers.getFirst(CLIENT_ID).equalsIgnoreCase("ANH")){
            return chain.filter(exchange);
        }
        if (headers.getFirst(SIGNATURE)==null){
            throw new InvalidRequestException(ApiGatewayError.HEADER_REQUIRED.apply(lang, List.of(SIGNATURE)));
        }
        if (headers.getFirst(TIMESTAMP)==null){
            throw new InvalidRequestException(ApiGatewayError.HEADER_REQUIRED.apply(lang, List.of(TIMESTAMP)));
        }
        String body = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
        String content = new StringBuilder(exchange.getRequest().getMethodValue()).append(headers.getFirst(TIMESTAMP))
                .append(headers.getFirst(CLIENT_ID)).append(headers).append(body).toString();
        if (RSAUtil.verify(content, headers.getFirst(SIGNATURE), RSAUtil.toPublicKey(PUBLIC_KEY))){
            throw new InvalidRequestException(ApiGatewayError.INVALID_SIGNATURE.apply(lang, null));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
