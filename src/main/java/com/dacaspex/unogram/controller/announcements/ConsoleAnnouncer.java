package com.dacaspex.unogram.controller.announcements;

import com.dacaspex.unogram.game.*;

public class ConsoleAnnouncer implements Announcer {
    private final String id;
    private final ConsoleFormatter formatter;

    public ConsoleAnnouncer(String id) {
        this.id = id;
        this.formatter = new ConsoleFormatter();
    }

    @Override
    public void gameCreated(String id, Player host) {
        print(String.format("Game created with id %s and host %s", id, host.getUsername()));
    }

    @Override
    public void playerJoinedParty(Player player, Party party) {
        print(String.format("Player %s joined the party", player.getUsername()));
    }

    @Override
    public void playerLeftParty(Player player, Party party) {
        print(String.format("Player %s left the party", player.getUsername()));
    }

    @Override
    public void playerAlreadyInParty(Player player, Party party) {
        print(String.format("Player %s already in the party", player.getUsername()));
    }

    @Override
    public void playerNotInParty(Player player, Party party) {
        print(String.format("Player %s is not in the party", player.getUsername()));
    }

    @Override
    public void gameStarted(UnoGame game) {
        print("Game has started");
    }

    @Override
    public void playedInvalidCard(Player player, Card card, UnoGame game) {
        print(
                String.format(
                        "Player %s played a %s card which is invalid",
                        player.getUsername(),
                        formatter.formatCard(card)
                )
        );
    }

    @Override
    public void playedBeforeTurn(Player player, UnoGame game) {
        print(
                String.format(
                        "Player %s played before his turn. The next in turn is %s",
                        player.getUsername(),
                        game.getParty().getCurrent().getUsername()
                )
        );
    }

    @Override
    public void playedCard(Player player, Card card, UnoGame game) {
        print(
                String.format(
                        "Player %s played a %s card. The top of the pile shows %s",
                        player.getUsername(),
                        formatter.formatCard(card),
                        formatter.formatCard(game.getDiscardPile().getCards().peek())
                )
        );
    }

    @Override
    public void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game) {
        print(
                String.format(
                        "Player %s played a %s card. The top of the pile shows %s. The chosen suit is %s",
                        player.getUsername(),
                        formatter.formatCard(card),
                        formatter.formatCard(game.getDiscardPile().getCards().peek()),
                        chosenSuit.name()
                )
        );
    }

    @Override
    public void drewCard(Player player, Card card, UnoGame game) {
        print(String.format("Player %s drew a %s card", player.getUsername(), formatter.formatCard(card)));
    }

    @Override
    public void gameFinished(UnoGame game) {
        print(String.format("Game finished. The winner is %s", game.getWinner().getUsername()));
    }

    private void print(String message) {
        System.out.println(String.format("[%s] %s", id, message));
    }
}
