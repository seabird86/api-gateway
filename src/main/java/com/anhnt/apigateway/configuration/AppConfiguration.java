package com.anhnt.apigateway.configuration;

import com.anhnt.apigateway.client.ConfigurationClient;
import com.anhnt.common.domain.configuration.response.Message;
import com.anhnt.common.domain.response.ConfigurationCache;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

@Configuration
@AllArgsConstructor
public class AppConfiguration {

    private ConfigurationClient client;

    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }

    @PostConstruct
    public void loadLocaleMessage(){
        ConfigurationCache.messageMap.putAll(client.getMessages().getData().getMessages().stream().collect(Collectors.toMap(Message::getCode, Message::toMessageMap)));
    }

}