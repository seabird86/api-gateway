package com.anhnt.apigateway.filter;

import com.anhnt.apigateway.configuration.AppProperty;
import com.anhnt.apigateway.constant.HeaderConstant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
@Import({RedisAutoConfiguration.class, RedisReactiveAutoConfiguration.class})
public class ClientRateLimiter{

    private ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME)
    private RedisScript<List<Long>> script;
    private AppProperty appProperty;

    public Mono<RateLimiter.Response> isAllowed(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String clientId = headers.getFirst(HeaderConstant.CLIENT_ID);
        AppProperty.ClientProperty clientProperty= appProperty.getClients().stream().filter(e->e.getName().equals(clientId)).findFirst().orElse(null);
        if (clientProperty==null){
            return Mono.just(new RateLimiter.Response(true, new HashMap<>()));
        }
        RedisRateLimiter.Config config = new RedisRateLimiter.Config().setReplenishRate(clientProperty.getRateLimiter().getReplenishRate())
                .setBurstCapacity(clientProperty.getRateLimiter().getBurstCapacity()).setRequestedTokens(clientProperty.getRateLimiter().getRequestedTokens());
        List<String> scriptArgs = List.of(
                config.getReplenishRate(),
                config.getBurstCapacity(),
                Instant.now().getEpochSecond(),
                config.getRequestedTokens()).stream().map(String::valueOf).toList();
        try{
            List<String> keys = List.of("request_rate_limiter."+clientId+".tokens", "request_rate_limiter."+clientId+".timestamp");
            Flux<List<Long>> flux = this.reactiveStringRedisTemplate.execute(this.script, keys, scriptArgs);
            return flux.onErrorResume((throwable) -> {
                    log.error("Rate-limiter exception:",throwable);
                    return Flux.just(Arrays.asList(1L, -1L));
                }).reduce(new ArrayList(), (longs, l) -> {
                    longs.addAll(l);
                    return longs;
                }).map((results) -> {
                    boolean allowed = (Long)results.get(0) == 1L;
                    Long tokensLeft = (Long)results.get(1);
                    return new RateLimiter.Response(allowed, this.getHeaders(config, tokensLeft));
                });
        } catch (Exception ex) {
            log.error("Rate-limiter exception:",ex);
            return Mono.just(new RateLimiter.Response(true, this.getHeaders(config, -1L)));
        }
    }

    public Map<String, String> getHeaders(RedisRateLimiter.Config config, Long tokensLeft) {
        return Map.of(RedisRateLimiter.REMAINING_HEADER, tokensLeft.toString(),RedisRateLimiter.REPLENISH_RATE_HEADER, String.valueOf(config.getReplenishRate()),RedisRateLimiter.BURST_CAPACITY_HEADER, String.valueOf(config.getBurstCapacity()),RedisRateLimiter.REQUESTED_TOKENS_HEADER, String.valueOf(config.getRequestedTokens()));
    }
}
