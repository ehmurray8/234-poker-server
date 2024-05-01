package dev.emurray.pokerserver.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

public class Deck extends Stack<Card> {

    public Deck() {
        super();
        addAllCards();
        Collections.shuffle(this);
    }

    private void addAllCards() {
        for (Suit suit : Suit.values()) {
            for (Rank rank :
                Arrays.stream(Rank.values()).filter(rank -> rank != Rank.ONE).toList()) {
                Card card = CardBuilder.builder().rank(rank).suit(suit).build();
                push(card);
            }
        }
    }
}
