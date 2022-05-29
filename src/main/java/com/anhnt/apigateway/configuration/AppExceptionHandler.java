package com.anhnt.apigateway.configuration;

import com.anhnt.common.domain.apigateway.response.ErrorEntityConstant;
import com.anhnt.common.domain.exception.AbstractException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class AppExceptionHandler extends AbstractErrorWebExceptionHandler {

    public AppExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction (ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(),this::toErrorResponse);
    }

    private Mono<ServerResponse> toErrorResponse (ServerRequest request) {
        Throwable throwable = getError(request);
        if (throwable instanceof AbstractException){
            return ServerResponse.status(((AbstractException) throwable).getError().getStatus())
                .contentType(MediaType.APPLICATION_JSON).bodyValue(((AbstractException) throwable).getError().toResponseEntity().getBody());
        }
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON).bodyValue(ErrorEntityConstant.INTERNAL_SERVER_ERROR.withParams(throwable.getMessage()).toResponseEntity().getBody());
    }
}
