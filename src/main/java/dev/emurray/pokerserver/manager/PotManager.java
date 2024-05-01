package dev.emurray.pokerserver.manager;

import dev.emurray.pokerserver.model.Player;
import dev.emurray.pokerserver.model.Pot;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PotManager {

    private final List<Pot> pots;

    private BigDecimal lastBetAmount;

    public PotManager(List<Pot> pots, BigDecimal lastBetAmount) {
        this.pots = pots;
        this.lastBetAmount = lastBetAmount;
    }

    public BigDecimal getLastBetAmount() {
        return lastBetAmount;
    }

    public void clearLastBetAmount() {
        lastBetAmount = BigDecimal.ZERO;
    }

    public Player playerAddMoney(Player player, BigDecimal amount) {
        var amountLeft = amount;
        var totalAmountOwedAllPots = BigDecimal.ZERO;
        var sidePotCreationIndex = -1;
        for (var pot : pots) {
            if (amountLeft.compareTo(BigDecimal.ZERO) > 0) {
                var amountPlayerPutInPot = pot.getAmountInPotFor(player.playerId(), BigDecimal.ZERO);
                var playerAmountNotInPot = pot.amountOwed().subtract(amountPlayerPutInPot);
                totalAmountOwedAllPots = totalAmountOwedAllPots.add(playerAmountNotInPot);
                if (pot.amountOwed().compareTo(amountPlayerPutInPot) > 0) {
                    if (amountLeft.compareTo(playerAmountNotInPot) == 0) {
                        pot.addAmountFor(player.playerId(), amountLeft);
                        amountLeft = BigDecimal.ZERO;
                    } else if (amountLeft.compareTo(playerAmountNotInPot) > 0) {
                        if (pot.isClosed()) {
                            pot.addAmountFor(player.playerId(), playerAmountNotInPot);
                            amountLeft = amountLeft.subtract(playerAmountNotInPot);
                        } else {
                            pot.addAmountFor(player.playerId(), amountLeft);
                            amountLeft = BigDecimal.ZERO;
                        }
                    } else {
                        sidePotCreationIndex = pots.indexOf(pot);
                        pot.close();
                        break;
                    }
                }
            }
        }
        if (amount.compareTo(totalAmountOwedAllPots) > 0) {
            lastBetAmount = amount.subtract(totalAmountOwedAllPots);
        }
        if (sidePotCreationIndex >= 0) {
            // TODO: I need to take money from other pots...
            var pot = new Pot();
            pot.addAmountFor(player.playerId(), amountLeft);
            pots.add(sidePotCreationIndex, pot);
        }

        if (amountLeft.compareTo(BigDecimal.ZERO) > 0) {
            for (var pot : pots) {
                if (!pot.isClosed()) {
                    pot.addAmountFor(player.playerId(), amountLeft);
                }
            }
        }

        return player.with(it ->
            it.balance(it.balance().subtract(amount)).amountThisTurn(it.amountThisTurn().add(amount))
        );
    }
}
