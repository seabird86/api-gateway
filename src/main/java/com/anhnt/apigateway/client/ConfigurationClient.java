package com.anhnt.apigateway.client;

import com.anhnt.apigateway.client.config.ConfigurationClientConfiguration;
import com.anhnt.common.domain.configuration.response.MessageResponse;
import com.anhnt.common.domain.response.BodyEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="configuration",url="${feign.client.config.configuration.url}",configuration = ConfigurationClientConfiguration.class)
public interface ConfigurationClient {
    @GetMapping(value = "/messages",produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    BodyEntity<MessageResponse> getMessages();
}