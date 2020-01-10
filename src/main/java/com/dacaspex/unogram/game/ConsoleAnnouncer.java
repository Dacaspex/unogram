package com.dacaspex.unogram.game;

public class ConsoleAnnouncer implements Announcer {
    @Override
    public void playerJoinedParty(Player player) {
        System.out.println("Player joined party");
    }

    @Override
    public void playerLeftParty(Player player) {
        System.out.println("Player left party");
    }

    @Override
    public void playerAlreadyInParty(Player player) {
        System.out.println("Player already in party");
    }

    @Override
    public void playerNotInParty(Player player) {
        System.out.println("Player not in party");
    }

    @Override
    public void gameStarted(UnoGame game) {
        System.out.println("Game has started");
    }

    @Override
    public void playedInvalidCard(Player player, Card card, UnoGame game) {
        System.out.println("Player played an invalid card");
    }

    @Override
    public void playedBeforeTurn(Player player, UnoGame game) {
        System.out.println("Player played before turn");
    }

    @Override
    public void playedCard(Player player, Card card, UnoGame game) {
        System.out.println("Player played a card");
    }

    @Override
    public void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game) {
        System.out.println("Player played a wild card");
    }

    @Override
    public void drewCard(Player player, Card card, UnoGame game) {
        System.out.println("Player drew a card");
    }
}
