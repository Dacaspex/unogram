package com.dacaspex.unogram.controller.announcements;

import com.dacaspex.unogram.game.Card;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.game.Suit;
import com.dacaspex.unogram.game.UnoGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompoundAnnouncer implements Announcer {
    public List<Announcer> announcers;

    public CompoundAnnouncer(Announcer... announcers) {
        this.announcers = new ArrayList<>(Arrays.asList(announcers));
    }

    @Override
    public void gameCreated(String id, Player host) {
        announcers.forEach(a -> a.gameCreated(id, host));
    }

    @Override
    public void playerJoinedParty(Player player, UnoGame game) {
        announcers.forEach(a -> a.playerJoinedParty(player, game));
    }

    @Override
    public void playerLeftParty(Player player, UnoGame game) {
        announcers.forEach(a -> a.playerLeftParty(player, game));
    }

    @Override
    public void gameAbandoned(UnoGame game) {
        announcers.forEach(a -> a.gameAbandoned(game));
    }

    @Override
    public void gameStarted(UnoGame game) {
        announcers.forEach(a -> a.gameStarted(game));
    }

    @Override
    public void playedInvalidCard(Player player, Card card, UnoGame game) {
        announcers.forEach(a -> a.playedInvalidCard(player, card, game));
    }

    @Override
    public void playedBeforeTurn(Player player, UnoGame game) {
        announcers.forEach(a -> a.playedBeforeTurn(player, game));
    }

    @Override
    public void playedCard(Player player, Card card, UnoGame game) {
        announcers.forEach(a -> a.playedCard(player, card, game));
    }

    @Override
    public void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game) {
        announcers.forEach(a -> a.playedWildCard(player, card, chosenSuit, game));
    }

    @Override
    public void drewCard(Player player, Card card, UnoGame game) {
        announcers.forEach(a -> a.drewCard(player, card, game));
    }

    @Override
    public void gameFinished(UnoGame game) {
        announcers.forEach(a -> a.gameFinished(game));
    }
}
