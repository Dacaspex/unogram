package com.dacaspex.unogram.controller.announcements;

import com.dacaspex.unogram.game.Card;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.game.Suit;
import com.dacaspex.unogram.game.UnoGame;

public interface Announcer {
    void gameCreated(String id, Player host);

    void playerJoinedParty(Player player, UnoGame game);

    void playerLeftParty(Player player, UnoGame game);

    void gameAbandoned(UnoGame game);

    void gameStarted(UnoGame game);

    void playedInvalidCard(Player player, Card card, UnoGame game);

    void playedBeforeTurn(Player player, UnoGame game);

    void playedCard(Player player, Card card, UnoGame game);

    void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game);

    void drewCard(Player player, Card card, UnoGame game);

    void gameFinished(UnoGame game);
}
