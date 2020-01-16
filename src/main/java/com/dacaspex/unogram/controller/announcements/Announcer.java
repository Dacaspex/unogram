package com.dacaspex.unogram.controller.announcements;

import com.dacaspex.unogram.game.*;

public interface Announcer {
    void gameCreated(String id, Player host);

    void playerJoinedParty(Player player, Party party);

    void playerLeftParty(Player player, Party party);

    void playerAlreadyInParty(Player player, Party party);

    void playerNotInParty(Player player, Party party);

    void gameStarted(UnoGame game);

    void playedInvalidCard(Player player, Card card, UnoGame game);

    void playedBeforeTurn(Player player, UnoGame game);

    void playedCard(Player player, Card card, UnoGame game);

    void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game);

    void drewCard(Player player, Card card, UnoGame game);

    void gameFinished(UnoGame game);
}
