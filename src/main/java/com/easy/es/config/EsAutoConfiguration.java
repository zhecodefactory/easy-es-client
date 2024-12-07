package com.easy.es.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EsConfigProperties.class)
public class EsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EsRestClient esRestClient(EsConfigProperties properties) {
        EsRestClient client = new EsRestClient(properties);
        client.init();
        return client;
    }
} 