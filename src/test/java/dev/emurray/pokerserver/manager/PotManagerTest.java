package dev.emurray.pokerserver.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.emurray.pokerserver.model.Player;
import dev.emurray.pokerserver.model.PlayerBuilder;
import dev.emurray.pokerserver.model.Pot;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

// TODO: Tests
public class PotManagerTest {

    @Test
    public void addMoneyToPot_FirstInPot() {
        var mainPot = new Pot();
        var pots = new ArrayList<>(List.of(mainPot));
        var potManager = new PotManager(pots, BigDecimal.ZERO);
        var player = defaultPlayer();
        var updatedPlayer = potManager.playerAddMoney(player, BigDecimal.ONE);
        assertEquals(BigDecimal.ONE, updatedPlayer.amountThisTurn());
        assertEquals(BigDecimal.ONE, mainPot.getAmountInPotFor(player.playerId(), null));
        assertEquals(BigDecimal.ONE, mainPot.amount());
        assertFalse(mainPot.isClosed());
        assertTrue(mainPot.canRaise());
    }

    @Test
    public void addMoneyToPot_Call() {

    }

    @Test
    public void addMoneyToPot_Raise() {

    }

    @Test
    public void addMoneyToPot_CreateSidePot_RepopenAction() {

    }

    @Test
    public void addMoneyToPot_CreateSidePot_DontReopenAction() {

    }

    @Test
    public void addMoneyToPot_CreateMultipleSidepots() {

    }

    private Player defaultPlayer() {
        return PlayerBuilder.builder()
            .amountThisTurn(BigDecimal.ZERO)
            .balance(new BigDecimal(100))
            .hasFolded(false)
            .playerId(UUID.randomUUID().toString())
            .avatar(UUID.randomUUID().toString())
            .build();
    }
}
