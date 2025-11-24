package com.example.buyme.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${product.service.url:http://localhost:8082}")
    private String productServiceUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl(productServiceUrl)
            .build();
    }
}
