package dev.emurray.pokerserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import dev.emurray.pokerserver.Game;
import dev.emurray.pokerserver.handler.EventListener;
import dev.emurray.pokerserver.manager.PotManager;
import dev.emurray.pokerserver.model.Rules;
import dev.emurray.pokerserver.model.RulesBuilder;
import dev.emurray.pokerserver.repository.PlayerRepository;
import dev.emurray.pokerserver.repository.SessionRepository;
import dev.emurray.pokerserver.service.ClientService;
import java.math.BigDecimal;
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

    private Thread gameThread;

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
        return ApplicationPropertiesBuilder.builder()
            .allowedOrigins(allowedOrigins)
            .cookieDomain(cookieDomain)
            .build();
    }

    @Bean
    @Qualifier(DISCONNECTED_SESSION_KEY)
    public Queue<String> disconnectedSessions() {
        return new ConcurrentLinkedQueue<>();
    }

    @Bean
    @Qualifier
    public Game game(
        Rules rules,
        SessionRepository sessionRepository,
        PlayerRepository playerRepository,
        @Qualifier(DISCONNECTED_SESSION_KEY) Queue<String> disconnectedSessions,
        ClientService clientService,
        PotManager potManager
    ) {
        Game game = new Game(rules, sessionRepository, playerRepository, disconnectedSessions, clientService, potManager);
        gameThread = new Thread(game::gameLoop);
        return game;
    }

    @Bean
    @Qualifier
    public Rules rules() {
        return RulesBuilder.builder()
            .bigBlindAmount(new BigDecimal(3))
            .smallBlindAmount(new BigDecimal(1))
            .actionWaitTime(30)
            .gameSelectionWaitTime(15)
            .build();
    }

    @Bean
    public EventBus eventBus(EventListener eventListener) {
        var eventBus = new EventBus();
        eventBus.register(eventListener);
        return eventBus;
    }
}
