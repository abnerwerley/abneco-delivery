package com.delivery.address.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AddressClient {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}