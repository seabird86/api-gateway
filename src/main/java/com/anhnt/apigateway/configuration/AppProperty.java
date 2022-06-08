package com.anhnt.apigateway.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@ConfigurationProperties("app")
@RefreshScope
@Validated
@Getter
@Setter
public class AppProperty {
    private List<ClientProperty> clients;
    @Getter
    @Setter
    public static class ClientProperty{
        private String name;
        private RateLimiterProperty rateLimiter;
        @Getter
        @Setter
        public static class RateLimiterProperty{
            private Integer replenishRate;
            private Integer burstCapacity;
            private Integer requestedTokens;
        }
    }
}