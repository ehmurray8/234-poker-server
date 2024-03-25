package dev.emurray.pokerserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final List<RequestHandler> requestHandlers;

    private final GlobalExceptionHandler globalExceptionHandler;

    public WebSocketConfig(
        ApplicationProperties applicationProperties,
        SessionRepository sessionRepository,
        ObjectMapper objectMapper,
        List<RequestHandler> requestHandlers,
        GlobalExceptionHandler globalExceptionHandler
    ) {
        this.applicationProperties = applicationProperties;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
        this.requestHandlers = requestHandlers;
        this.globalExceptionHandler = globalExceptionHandler;
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
            requestHandlers,
            globalExceptionHandler
        );
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        return new ServletServerContainerFactoryBean();
    }
}
