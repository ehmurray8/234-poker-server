package dev.emurray.pokerserver.controller;

import dev.emurray.pokerserver.Game;
import dev.emurray.pokerserver.config.ApplicationProperties;
import dev.emurray.pokerserver.model.GameState;
import io.micrometer.common.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {

    public static final String SESSION_COOKIE_NAME = "234-poker-session";

    private final ApplicationProperties applicationProperties;

    private final Game game;

    public SessionController(ApplicationProperties applicationProperties, Game game) {
        this.applicationProperties = applicationProperties;
        this.game = game;
    }

    @PostMapping("/session")
    public CompletableFuture<ResponseEntity<Void>> createSession(
        @CookieValue(value = SESSION_COOKIE_NAME, required = false) String sessionId
    ) {
        if (StringUtils.isEmpty(sessionId)) {
            var sessionCookie = ResponseCookie.fromClientResponse(SESSION_COOKIE_NAME, UUID.randomUUID().toString())
                .domain(applicationProperties.cookieDomain())
                .build();
            var headers = CollectionUtils.toMultiValueMap(Map.of(
                HttpHeaders.SET_COOKIE, List.of(sessionCookie.toString())
            ));
            return CompletableFuture.completedFuture(new ResponseEntity<>(headers, HttpStatus.OK));
        } else {
            return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.OK));
        }
    }

    @PostMapping("/get-session-state")
    public CompletableFuture<ResponseEntity<GameState>> getSessionState(
        @CookieValue(SESSION_COOKIE_NAME) String sessionId
    ) {
        var gameState = game.getState(sessionId);
        if (gameState.isPresent()) {
            return CompletableFuture.completedFuture(ResponseEntity.ok(gameState.get()));
        } else {
            return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
        }
    }
}
