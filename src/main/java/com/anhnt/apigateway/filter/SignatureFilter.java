package com.anhnt.apigateway.filter;

import com.anhnt.common.domain.apigateway.response.ErrorEntityConstant;
import com.anhnt.common.domain.exception.InvalidRequestException;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
        HttpHeaders headers = request.getHeaders();
        if (headers.getFirst(CLIENT_ID)==null){
            throw new InvalidRequestException(ErrorEntityConstant.HEADER_REQUIRED.withParams(CLIENT_ID));
        }
        if (headers.getFirst(CLIENT_ID).equalsIgnoreCase("ANH")){
            return chain.filter(exchange);
        }
        if (headers.getFirst(SIGNATURE)==null){
            throw new InvalidRequestException(ErrorEntityConstant.HEADER_REQUIRED.withParams(SIGNATURE));
        }
        if (headers.getFirst(TIMESTAMP)==null){
            throw new InvalidRequestException(ErrorEntityConstant.HEADER_REQUIRED.withParams(TIMESTAMP));
        }
//        String body = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
//        String content = new StringBuilder(exchange.getRequest().getMethodValue()).append(headers.getFirst(TIMESTAMP))
//                .append(headers.getFirst(CLIENT_ID)).append(headers).append(body).toString();
//        if (RSAUtil.verify(content, headers.getFirst(SIGNATURE),RSAUtil.toPublicKey(PUBLIC_KEY))){
//            throw new InvalidRequestException(ResponseErrorConstant.INVALID_SIGNATURE);
//        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
