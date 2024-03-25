package dev.emurray.pokerserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    public static final String DISCONNECTED_SESSION_KEY = "disconnected-sessions";

    @Value("${poker-server.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${poker-server.cookie-domain}")
    private String cookieDomain;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ApplicationProperties applicationProperties() {
        return ImmutableApplicationProperties.builder()
            .allowedOrigins(allowedOrigins)
            .cookieDomain(cookieDomain)
            .build();
    }

    @Bean
    @Qualifier(DISCONNECTED_SESSION_KEY)
    public Queue<String> disconnectedSessions() {
        return new ConcurrentLinkedQueue<>();
    }
}
