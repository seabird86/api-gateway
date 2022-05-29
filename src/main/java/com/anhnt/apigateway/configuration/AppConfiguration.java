package com.anhnt.apigateway.configuration;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }

}