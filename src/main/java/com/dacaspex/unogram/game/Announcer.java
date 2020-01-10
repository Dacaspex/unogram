package com.dacaspex.unogram.game;

public interface Announcer {
    void playerJoinedParty(Player player);

    void playerLeftParty(Player player);

    void playerAlreadyInParty(Player player);

    void playerNotInParty(Player player);

    void gameStarted(Party party, UnoGame game);

    void playedInvalidCard(Player player, Card card, UnoGame game);

    void playedCard(Player player, Card card, UnoGame game);

    void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game);

    void drewCard(Player player, Card card, UnoGame game);
}
