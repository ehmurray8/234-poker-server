package dev.emurray.pokerserver.model;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Pot {
    private BigDecimal amount;

    private final Map<String, BigDecimal> playerIdToAmountInPot;

    private BigDecimal amountOwed;

    private boolean isClosed;

    private boolean canRaise;

    public Pot() {
        amount = BigDecimal.ZERO;
        playerIdToAmountInPot = new HashMap<>();
        amountOwed = BigDecimal.ZERO;
        isClosed = false;
        canRaise = true;
    }

    public BigDecimal amount() {
        return amount;
    }

    public BigDecimal amountOwed() {
        return amountOwed;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public boolean canRaise() {
        return canRaise;
    }

    public BigDecimal getAmountInPotFor(String playerId, BigDecimal defaultValue) {
        return playerIdToAmountInPot.getOrDefault(playerId, defaultValue);
    }

    public BigDecimal addAmountFor(String playerId, BigDecimal newAmount) {
        amount = amount.add(newAmount);
        var currentAmount = playerIdToAmountInPot.getOrDefault(playerId, BigDecimal.ZERO);
        var amountThisTurn = currentAmount.add(newAmount);
        if (amountThisTurn.compareTo(amountOwed) > 0) {
            amountOwed = amountThisTurn;
        }
        return playerIdToAmountInPot.put(playerId, amountThisTurn);
    }

    public void close() {
        isClosed = true;
    }

    public void setCanRaise(boolean canRaise) {
        this.canRaise = canRaise;
    }
}
