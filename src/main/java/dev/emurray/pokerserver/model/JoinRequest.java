package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableJoinRequest.class)
@JsonDeserialize(as = ImmutableJoinRequest.class)
public interface JoinRequest {
    String name();

    String avatar();
}
