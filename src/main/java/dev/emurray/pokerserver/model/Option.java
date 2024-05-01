package dev.emurray.pokerserver.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.math.BigDecimal;
import javax.annotation.Nullable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
@RecordBuilder
public record Option(
    OptionType optionType,
    BigDecimal amount,
    @Nullable BigDecimal maxAmount,
    @Nullable BigDecimal step
) { }
