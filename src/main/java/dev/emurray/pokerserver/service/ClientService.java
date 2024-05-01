package dev.emurray.pokerserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import dev.emurray.pokerserver.model.GameState;
import dev.emurray.pokerserver.model.GameType;
import dev.emurray.pokerserver.model.GameTypeSelectRequestBuilder;
import dev.emurray.pokerserver.model.GameTypeSelectResponse;
import dev.emurray.pokerserver.model.GameTypeSelectResponseBuilder;
import dev.emurray.pokerserver.model.MessageType;
import dev.emurray.pokerserver.model.Option;
import dev.emurray.pokerserver.model.OptionBuilder;
import dev.emurray.pokerserver.model.OptionSelectRequestBuilder;
import dev.emurray.pokerserver.model.OptionSelectResponse;
import dev.emurray.pokerserver.model.OptionSelectResponseBuilder;
import dev.emurray.pokerserver.model.OptionType;
import dev.emurray.pokerserver.model.Player;
import dev.emurray.pokerserver.model.Rules;
import dev.emurray.pokerserver.model.SocketEvent;
import dev.emurray.pokerserver.repository.PlayerRepository;
import dev.emurray.pokerserver.repository.SessionRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.spi.LoggerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private static final MathContext MATH_CONTEXT = new MathContext(2);

    private final PlayerRepository playerRepository;

    private final SessionRepository sessionRepository;

    private final ScheduledExecutorService scheduledExecutorService;

    private final Random random;

    private final EventBus eventBus;

    private final ObjectMapper objectMapper;


    public ClientService(
        PlayerRepository playerRepository,
        SessionRepository sessionRepository,
        ScheduledExecutorService scheduledExecutorService,
        Random random,
        EventBus eventBus,
        ObjectMapper objectMapper
    ) {
        this.playerRepository = playerRepository;
        this.sessionRepository = sessionRepository;
        this.scheduledExecutorService = scheduledExecutorService;
        this.random = random;
        this.eventBus = eventBus;
        this.objectMapper = objectMapper;
    }

    public void requestGameType(Player player, Rules rules) {
        var sessionId = playerRepository.getSessionId(player.playerId());
        if (sessionId.isEmpty()) {
            throw new IllegalStateException("No sessionId for player, " + player.playerId());
        }
        if (player.isCpu()) {
            Runnable runnable = () -> {
                try {
                    var response = GameTypeSelectResponseBuilder.builder()
                        .gameTypeSelection(getRandomGameType().getValue())
                        .build();
                    var message = objectMapper.writeValueAsString(response);
                    var event = new SocketEvent(
                        null, sessionId.get(), MessageType.GAME_TYPE_SELECT_RESPONSE, message
                    );
                    eventBus.post(event);
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize game type request for CPU.", e);
                }
            };
            scheduledExecutorService.schedule(
                runnable, generateDelaySeconds(rules.gameSelectionWaitTime()), TimeUnit.SECONDS
            );
        } else {
            var gameTypeSelectRequest = GameTypeSelectRequestBuilder.builder()
                .gameTypeOptions(Arrays.stream(GameType.values()).map(GameType::getValue).toList())
                .build();
            sessionRepository.sendMessage(
                sessionId.get(), MessageType.GAME_TYPE_SELECT_REQUEST, gameTypeSelectRequest
            );
        }
    }

    public void requestOption(Player player, Rules rules, List<Option> options) {
        var sessionId = playerRepository.getSessionId(player.playerId());
        if (sessionId.isEmpty()) {
            throw new IllegalStateException("No sessionId for player, " + player.playerId());
        }
        if (player.isCpu()) {
            Runnable runnable = () -> {
                try {
                    var response = OptionSelectResponseBuilder.builder()
                        .selectedOption(getRandomOption(options))
                        .build();
                    var message = objectMapper.writeValueAsString(response);
                    var event = new SocketEvent(
                        null, sessionId.get(), MessageType.OPTION_SELECT_RESPONSE, message
                    );
                    eventBus.post(event);
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize game type request for CPU.", e);
                }
            };
            scheduledExecutorService.schedule(
                runnable, generateDelaySeconds(rules.actionWaitTime()), TimeUnit.SECONDS
            );
        } else {
            var request = OptionSelectRequestBuilder.builder().options(options).build();
            sessionRepository.sendMessage(
                sessionId.get(), MessageType.OPTION_SELECT_REQUEST, request
            );
        }
    }

    public void sendGameState(GameState gameState, Player player) {
        var sessionId = playerRepository.getSessionId(player.playerId());
        if (sessionId.isEmpty()) {
            throw new IllegalStateException("No sessionId for player, " + player.playerId());
        }
        if (!player.isCpu()) {
            sessionRepository.sendMessage(sessionId.get(), MessageType.GAME_STATE_UPDATE, gameState);
        }
    }

    private long generateDelaySeconds(long maxNumber) {
        var offset = Math.round(maxNumber * 0.1);
        var probability = random.nextDouble();
        var result = (long) Math.ceil(offset + ((maxNumber - (2 * offset)) * Math.log(1 - probability)));
        result = Math.min(result, maxNumber - offset);
        return Math.max(result, offset);
    }

    private GameType getRandomGameType() {
        return GameType.values()[random.nextInt(GameType.values().length)];
    }

    private Option getRandomOption(List<Option> options) {
        var randomInt = random.nextInt(100);
        if (options.size() == 2) {
            if (randomInt < 65) {
                return options.getFirst();
            } else {
                return options.getLast();
            }
        } else if (options.size() == 3) {
            if (randomInt < 20) {
                return options.getFirst();
            } else if (randomInt < 80) {
                return options.get(1);
            } else {
                return OptionBuilder.builder()
                    .amount(getRaiseAmount(options.getLast()))
                    .optionType(OptionType.RAISE)
                    .build();
            }
        } else {
            try {
                var optionString = objectMapper.writeValueAsString(options);
                throw new IllegalArgumentException("Unknown number of options. " + optionString);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to serialize invalid option string.", e);
            }
        }
    }

    private BigDecimal getRaiseAmount(Option option) {
        if (option.maxAmount() == null || option.step() == null) {
            return option.amount();
        }
        var min = option.amount();
        var max = option.maxAmount();
        var step = option.step();

        var range = max.subtract(min).divide(step, MATH_CONTEXT).add(BigDecimal.ONE);
        var randomIndex = random.nextLong(range.longValue());

        return min.add(new BigDecimal(randomIndex).multiply(step));
    }
}
