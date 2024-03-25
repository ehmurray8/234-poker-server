package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableErrorResponse.class)
@JsonDeserialize(as = ImmutableErrorResponse.class)
public interface ErrorResponse {
    String message();

    int code();

    String detailCode();
}
