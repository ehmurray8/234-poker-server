package dev.emurray.pokerserver;

import static dev.emurray.pokerserver.config.ApplicationConfig.DISCONNECTED_SESSION_KEY;

import dev.emurray.pokerserver.manager.PotManager;
import dev.emurray.pokerserver.model.Card;
import dev.emurray.pokerserver.model.Deck;
import dev.emurray.pokerserver.model.GameState;
import dev.emurray.pokerserver.model.GameStateBuilder;
import dev.emurray.pokerserver.model.GameType;
import dev.emurray.pokerserver.model.MessageType;
import dev.emurray.pokerserver.model.Option;
import dev.emurray.pokerserver.model.OptionBuilder;
import dev.emurray.pokerserver.model.OptionType;
import dev.emurray.pokerserver.model.Player;
import dev.emurray.pokerserver.model.Pot;
import dev.emurray.pokerserver.model.Rules;
import dev.emurray.pokerserver.repository.PlayerRepository;
import dev.emurray.pokerserver.repository.SessionRepository;
import dev.emurray.pokerserver.service.ClientService;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

public class Game {

    private static final Logger log = LoggerFactory.getLogger(Game.class);

    public static final int TABLE_SIZE = 6;

    private final Queue<Player> waitingPlayers;

    private final Player[] playersInGame;

    private final Rules rules;

    private final Queue<String> disconnectedSessions;

    private final SessionRepository sessionRepository;

    private final PlayerRepository playerRepository;

    private final Map<String, List<Card>> playerIdToHands;

    private int buttonPosition;

    private GameType gameType;

    private Option selectedOption;

    private final Object gameTypeLock;

    private final Object optionLock;

    private String gameTypeSelectionPlayerId;

    private String optionSelectionPlayerId;

    private ClientService clientService;

    private final List<Card> communityCards;

    private Deck deck;

    private List<Pot> pots;

    private final PotManager potManager;

    public Game(
        Rules rules,
        SessionRepository sessionRepository,
        PlayerRepository playerRepository,
        @Qualifier(DISCONNECTED_SESSION_KEY) Queue<String> disconnectedSessions,
        ClientService clientService,
        PotManager potManager
    ) {
        waitingPlayers = new ArrayDeque<>();
        playersInGame = new Player[TABLE_SIZE];
        playerIdToHands = new HashMap<>();
        buttonPosition = 0;
        gameTypeLock = new Object();
        optionLock = new Object();
        communityCards = new ArrayList<>();
        deck = new Deck();
        pots = new ArrayList<>();
        this.rules = rules;
        this.disconnectedSessions = disconnectedSessions;
        this.sessionRepository = sessionRepository;
        this.playerRepository = playerRepository;
        this.clientService = clientService;
        this.potManager = potManager;
    }

    public void addPlayer(String sessionId, Player player) {
        disconnectedSessions.remove(sessionId);
        if (Arrays.stream(playersInGame).filter(Objects::nonNull).noneMatch(it -> it.equals(player))
            && !waitingPlayers.contains(player)
        ) {
            waitingPlayers.add(player);
        }
        sessionRepository.sendMessage(sessionId, MessageType.JOIN_RESPONSE, getState(sessionId).get());
    }

    public Optional<GameState> getState(String sessionId) {
        var builder = GameStateBuilder.builder()
            .numCards(gameType.getNumCards())
            .table(Arrays.asList(playersInGame))
            .seatPosition(-1)
            .hand(List.of())
            .tableSize(TABLE_SIZE)
            .buttonPosition(buttonPosition)
            .bigBlindAmount(rules.bigBlindAmount())
            .potAmount(getTotalAmount());
        var player = playerRepository.get(sessionId);
        if (player.isPresent()) {
            if (waitingPlayers.contains(player.get())) {
                return Optional.of(builder.build());
            } else {
                for (int i = 0; i < playersInGame.length; i++) {
                    if (player.get().equals(playersInGame[i])) {
                        builder = builder.seatPosition(i);
                        var hand = Optional.ofNullable(playerIdToHands.get(player.get().playerId()))
                            .orElse(List.of());
                        builder = builder.hand(hand);
                        return Optional.of(builder.build());
                    }
                }
            }
        }
        return Optional.empty();
    }

    public void selectGameType(GameType gameType, String playerId) {
        synchronized (gameTypeLock) {
            if (playerId != null && playerId.equals(gameTypeSelectionPlayerId)) {
                gameTypeSelectionPlayerId = null;
                this.gameType = gameType;
                gameTypeLock.notifyAll();
            }
        }
    }

    public void selectOption(Option option, String playerId) {
        synchronized (optionLock) {
            var player = Arrays.stream(playersInGame)
                .filter(it -> it.playerId() != null && it.playerId().equals(playerId))
                .findFirst();
            if (player.isPresent()
                && playerId.equals(optionSelectionPlayerId)
                && isOptionValid(player.get(), option)
            ) {
                optionSelectionPlayerId = null;
                selectedOption = option;
                optionLock.notifyAll();
            }
        }
    }

    private boolean isOptionValid(Player player, Option option) {
        var options = generateOptions(player);
        return options.contains(option) || isValidRaiseOption(options, option);
    }

    private boolean isValidRaiseOption(List<Option> options, Option option) {
        var raiseOption = options.stream()
            .filter(it -> OptionType.RAISE.equals(it.optionType()))
            .findFirst();
        return raiseOption.filter(value ->
                option.amount().compareTo(value.amount()) >= 0
                && option.amount().compareTo(value.maxAmount()) <= 0
                && option.amount().remainder(option.step()).compareTo(BigDecimal.ZERO) == 0
            )
            .isPresent();
    }

    public void gameLoop() {
        // TODO: Implement gameLoop
        // TODO: Ask broke players to re-join - Ask them to re-join
        // TODO: Send any broke players that did not re-join back to join form
        // Remove any disconnected players ✅
        // Check how many players are waiting room, and remove any CPU players as needed ✅
        // Add players from waiting room to in-game ✅
        // Fill the rest of the game with CPU players ✅

        // Ask dealer for game choice ✅
        // Blinds ✅
        // Deal ✅
        // preflop
        // turn
        // river

        // Pay winners

        while (true) {
            playerIdToHands.clear();
            gameType = null;
            for (var i = 0; i < playersInGame.length; i++) {
                playersInGame[i] = playersInGame[i].with(it ->
                    it.hasFolded(true).amountThisTurn(BigDecimal.ZERO)
                );
            }

            removeAllDisconnectedPlayers();
            replaceCpuPlayers();
            addCpuPlayers();

            deck = new Deck();
            handlePreFlop();
            handleAction();
        }
    }

    private void removeAllDisconnectedPlayers() {
        disconnectedSessions.forEach(this::disconnectPlayer);
        disconnectedSessions.clear();
    }

    private void replaceCpuPlayers() {
        for (var i = 0; i < playersInGame.length; i++) {
            if (playersInGame[i].isCpu() && !waitingPlayers.isEmpty()) {
                var playerToRemove = playersInGame[i];
                playersInGame[i] = waitingPlayers.remove();
                playerRepository.removePlayer(playerToRemove.playerId());
            }
        }
    }

    private void addCpuPlayers() {
        for (var i = 0; i < playersInGame.length; i++) {
            if (playersInGame[i] == null) {
                playersInGame[i] = playerRepository.createCpuPlayer(i);
            }
        }
    }

    private Option askPlayerForOption(Player player, List<Option> options) {
        optionSelectionPlayerId = player.playerId();
        Option option = null;
        try {
            clientService.requestOption(player, rules, options);
            synchronized (optionLock) {
                while (selectedOption == null) {
                    optionLock.wait(rules.actionWaitTime() * 1_000L);
                }
            }
            option = selectedOption;
            selectedOption = null;
        } catch (IllegalStateException | InterruptedException e) {
            log.error("Failed to ask player for option", e);
        }
        return option == null ? options.getFirst() : option;
    }

    private void askPlayerForGameType(Player player) {
        gameTypeSelectionPlayerId = player.playerId();
        GameType type = null;
        try {
            clientService.requestGameType(player, rules);
            synchronized (gameTypeLock) {
                while (gameType == null) {
                    gameTypeLock.wait(rules.gameSelectionWaitTime() * 1_000L);
                }
                type = gameType;
            }
        } catch (IllegalStateException | InterruptedException e) {
            log.error("Failed to ask player for gameType", e);
        }
        gameType = type == null ? GameType.HOLD_EM : type;
    }

    private void deal(Deck deck) {
        for (int i = 0; i < playersInGame.length * 2; i++) {
            var player = playersInGame[i % 2];
            if (player != null && player.isSittingOut()) {
                var playerId = player.playerId();
                var cards = playerIdToHands.computeIfAbsent(playerId, it -> new ArrayList<>());
                cards.add(deck.pop());
            }
        }
    }

    private void sendGameState() {
        for (var player : playersInGame) {
            var sessionId = playerRepository.getSessionId(player.playerId());
            if (sessionId.isPresent()) {
                var gameState = getState(sessionId.get());
                if (gameState.isPresent()) {
                    clientService.sendGameState(gameState.get(), player);
                } else {
                    log.error("No gameState for session, " + sessionId);
                }
            } else {
                log.error("No sessionId for player, " + player.playerId());
            }
        }
    }

    void handleAction() {
        handlePreFlop();
        if (stillBetting()) {
            handleFlop();
        }
        if (stillBetting()) {
            handleTurn();
        }
        if (stillBetting()) {
            handleRiver();
        }
        // Pay Winners
        // currentHand.payWinners();
    }

    private boolean stillBetting() {
        return Arrays.stream(playersInGame).filter(player -> !player.hasFolded()).toList().size() > 1;
    }

    private void handlePreFlop() {
        pots.add(new Pot());
        potManager.playerAddMoney(playersInGame[(buttonPosition + 1) % TABLE_SIZE], rules.smallBlindAmount());
        potManager.playerAddMoney(playersInGame[(buttonPosition + 2) % TABLE_SIZE], rules.bigBlindAmount());
        sendGameState();
        askPlayerForGameType(playersInGame[buttonPosition]);
        deal(deck);
        sendGameState();

        var currentAction = findStartingLocation();
        bettingRound();
//        var bigBlindPlayer = players[bigBlindNum()];
//        if (currentAction == bigBlindNum()
//            && bigBlindPlayer.getAmountThisTurn() == rules.getBigBlind()) {
//            currentHand.setupBetRound();
//            List<Option> currOptions = currentHand.generateOptions(bigBlindPlayer);
//            Option option = askPlayerForOption(currOptions, bigBlindPlayer);
//            currentHand.executeOption(bigBlindPlayer, option);
//            incrementCurrentAction();
//            if (option.getType() != Option.OptionType.CHECK) {
//                bettingRound(false);
//            }
//        }
    }

    private void handleFlop() {
        deck.pop();
        IntStream.range(0, 3).forEach(iteration -> communityCards.add(deck.pop()));
        bettingRound();
    }

    private void handleTurn() {
        deck.pop();
        communityCards.add(deck.pop());
        bettingRound();
    }

    private void handleRiver() {
        deck.pop();
        communityCards.add(deck.pop());
        bettingRound();
    }

    private int findStartingLocation() {
        int num = buttonPosition + 1;
        do {
            var player = playersInGame[num % TABLE_SIZE];
            if (player.amountThisTurn().compareTo(BigDecimal.ZERO) > 0
                && !player.isSittingOut() && !player.hasFolded()
            ) {
                return num;
            }
            num++;
        } while (true);
    }

    private int incrementAction(int currentAction) {
        int num = currentAction + 1;
        do {
            var player = playersInGame[num % TABLE_SIZE];
            if (!player.isSittingOut() && !player.hasFolded()
            ) {
                return num;
            }
            num++;
        } while (true);
    }

    private void bettingRound() {
        var currentAction = findStartingLocation();
        List<Option> currentOptions;
        while (playersBetting()) {
            var player = playersInGame[currentAction];
            currentOptions = generateOptions(player);
            var option = askPlayerForOption(player, currentOptions);
            // TODO
//            executeOption(player, option);
            currentAction = incrementAction(currentAction);
        }
        // Reset betting now
        // Set all players to have -1 amountThisTurn to allow for playersBetting to return false in a check-around scenario
    }

    // TODO: I think that there really needs to be sessionId -> user mapping
    // And then a user maps to player, that way player can have a userId
//    private void executeOption(Player player, Option option) {
//        var type = option.optionType();
//        var currentPot = pots.getLast();
//        switch (type) {
//            case FOLD:
//                player.fold();
//                players.remove(player);
//                break;
//            case CHECK:
//                player.check();
//                break;
//            case RAISE:
//            case ALLIN:
//            case BET:
//                var startingAmountThisTurn = player.setupRaise();
//                if (startingAmountThisTurn > 0) {
//                    currentPot.removeAmount(startingAmountThisTurn);
//                    lastRaiseAmount = option.getAmount() - startingAmountThisTurn;
//                } else {
//                    lastRaiseAmount = option.getAmount();
//                }
//                var amountOwed = option.getAmount();
//                currentPot.setAmountOwed(amountOwed);
//                chargeAmount(amountOwed, Collections.singletonList(player));
//                break;
//            case CALL:
//                chargeAmount(option.getAmount(), Collections.singletonList(player));
//                break;
//            default:
//                break;
//        }
//    }

    // TODO
    private List<Option> generateOptions(Player player) {
        var amountOwed = pots.stream().map(Pot::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (player.amountThisTurn().compareTo(BigDecimal.ZERO) > 0) {
            amountOwed = amountOwed.subtract(player.amountThisTurn());
        }
        if (amountOwed.compareTo(BigDecimal.ZERO) == 0) {
            return createCheckAndBetOptions(player);
        } else {
            return createFoldCallRaiseOptions(player, amountOwed);
        }
    }

    private List<Option> createCheckAndBetOptions(Player player) {
        var options = new ArrayList<Option>();
        options.add(OptionBuilder.builder()
            .optionType(OptionType.CHECK)
            .amount(BigDecimal.ZERO)
            .build()
        );
        options.add(OptionBuilder.builder()
            .amount(rules.bigBlindAmount().min(player.balance()))
            .build()
        );
        return options;
    }

    private List<Option> createFoldCallRaiseOptions(Player player, BigDecimal amountOwed) {
        var options = new ArrayList<Option>();
        options.add(OptionBuilder.builder()
            .amount(BigDecimal.ZERO)
            .optionType(OptionType.FOLD)
            .build()
        );
        options.add(OptionBuilder.builder()
            .optionType(OptionType.CALL)
            .amount(amountOwed)
            .build());
        if (amountOwed.add(potManager.getLastBetAmount()).compareTo(player.balance()) < 0) {
            int count = (int) Arrays.stream(playersInGame)
                .filter(p -> p.balance().compareTo(BigDecimal.ZERO) > 0 && !p.hasFolded() && !p.isSittingOut())
                .count();
            if (count > 1) {
                var amountThisTurn = player.amountThisTurn();
                if (amountThisTurn.compareTo(BigDecimal.ZERO) < 0) {
                    amountThisTurn = BigDecimal.ZERO;
                }
                var raiseAmount = amountThisTurn.add(amountOwed).add(potManager.getLastBetAmount());
                options.add(OptionBuilder.builder()
                    .amount(raiseAmount)
                    .optionType(OptionType.RAISE)
                    .build()
                );
            }
        }
        return options;
    }

    // TODO
    private boolean playersBetting() {
        var numPlayersInHand = Arrays.stream(playersInGame)
            .filter(it -> !it.hasFolded())
            .filter(it -> !it.isSittingOut())
            .filter(it -> it.balance().compareTo(BigDecimal.ZERO) > 0)
            .toList()
            .size();
        if (numPlayersInHand < 2) {
            return false;
        }

        var amountOwed = pots.stream()
            .filter(it -> !it.isClosed()).map(Pot::amountOwed)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        var numPlayersWithMoney = Arrays.stream(playersInGame)
            .filter(it -> it.balance().compareTo(BigDecimal.ZERO) > 0)
            .count();
        boolean playerNoAction = Arrays.stream(playersInGame)
            .anyMatch(it -> it.amountThisTurn().compareTo(BigDecimal.ONE.negate()) == 0);

        boolean atLeastOnePlayerNoAction = numPlayersWithMoney > 1 && playerNoAction;
        boolean playersOweMoney = amountOwed.compareTo(BigDecimal.ZERO) > 0;
        return numPlayersWithMoney > 0 && (playersOweMoney || atLeastOnePlayerNoAction);
    }

    private void disconnectPlayer(String sessionId) {
        var player = playerRepository.get(sessionId);
        if (player.isPresent()) {
            waitingPlayers.remove(player.get());
            removePlayerFromGame(player.get());
        }
    }

    private void removePlayerFromGame(Player player) {
        for (var i = playersInGame.length - 1; i >= 0; i--) {
            if (player.equals(playersInGame[i])) {
                playersInGame[i] = null;
            }
        }
    }

    private BigDecimal getTotalAmount() {
        return pots.stream()
            .map(Pot::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
