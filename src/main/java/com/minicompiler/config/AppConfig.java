package com.minicompiler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central Spring application configuration.
 * Registers application-wide beans shared across all components.
 */
@Configuration
public class AppConfig {

    /**
     * Configures the global {@link ObjectMapper} with Java time support
     * and timestamp serialization disabled.
     *
     * @return a fully configured {@link ObjectMapper} instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return mapper;
    }
}