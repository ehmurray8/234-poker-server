package dev.emurray.pokerserver.config;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface ApplicationProperties {

    List<String> allowedOrigins();

    String cookieDomain();
}
