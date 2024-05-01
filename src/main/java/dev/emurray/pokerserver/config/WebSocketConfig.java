package dev.emurray.pokerserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import dev.emurray.pokerserver.handler.GlobalExceptionHandler;
import dev.emurray.pokerserver.handler.PokerSocketHandler;
import dev.emurray.pokerserver.handler.RequestHandler;
import dev.emurray.pokerserver.repository.SessionRepository;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ApplicationProperties applicationProperties;

    private final SessionRepository sessionRepository;

    private final ObjectMapper objectMapper;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final EventBus eventBus;

    public WebSocketConfig(
        ApplicationProperties applicationProperties,
        SessionRepository sessionRepository,
        ObjectMapper objectMapper,
        GlobalExceptionHandler globalExceptionHandler,
        EventBus eventBus
    ) {
        this.applicationProperties = applicationProperties;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
        this.globalExceptionHandler = globalExceptionHandler;
        this.eventBus = eventBus;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
            .addHandler(pokerSocketHandler(), "/socket")
            .setAllowedOrigins(applicationProperties.allowedOrigins().toArray(new String[]{}))
            .addInterceptors(new HttpSessionHandshakeInterceptor());
    }

    @Bean
    public WebSocketHandler pokerSocketHandler() {
        return new PokerSocketHandler(
            sessionRepository,
            objectMapper,
            globalExceptionHandler,
            eventBus
        );
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        return new ServletServerContainerFactoryBean();
    }
}
