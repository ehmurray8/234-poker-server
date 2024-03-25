package dev.emurray.pokerserver.controller;

import dev.emurray.pokerserver.config.ApplicationProperties;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {

    public static final String SESSION_COOKIE_NAME = "234-poker-session";

    private final ApplicationProperties applicationProperties;

    public SessionController(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @PostMapping("/session")
    public CompletableFuture<ResponseEntity<Void>> createSession() {
        var sessionCookie = ResponseCookie.fromClientResponse(SESSION_COOKIE_NAME, UUID.randomUUID().toString())
            .domain(applicationProperties.cookieDomain())
            .build();
        var headers = CollectionUtils.toMultiValueMap(Map.of(
            HttpHeaders.SET_COOKIE, List.of(sessionCookie.toString())
        ));
        return CompletableFuture.completedFuture(new ResponseEntity<>(headers, HttpStatus.OK));
    }
}
