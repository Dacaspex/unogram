package com.dacaspex.unogram.game;

import com.dacaspex.unogram.main.MessageFormatter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class TelegramAnnouncer implements Announcer {

    private final TelegramLongPollingBot bot;
    private final MessageFormatter formatter;
    private Chat groupChat;
    private Map<Player, User> userMap;
    private Map<Player, Chat> chatMap;

    public TelegramAnnouncer(TelegramLongPollingBot bot) {
        this.bot = bot;
        this.groupChat = null;
        this.userMap = new HashMap<>();
        this.chatMap = new HashMap<>();
        this.formatter = new MessageFormatter();
    }

    public void setGroupChat(Chat groupChat) {
        this.groupChat = groupChat;
    }

    public void addUserMapping(User user, Player player, Chat chat) {
        this.userMap.put(player, user);
        this.chatMap.put(player, chat);
    }

    @Override
    public void playerJoinedParty(Player player) {
        long playerChatId = chatMap.get(player).getId();
        User user = userMap.get(player);

        SendMessage personalMessage = new SendMessage()
                .setChatId(playerChatId)
                .setText("You successfully joined this game of Uno! Your cards and actions will appear in this space. " +
                        "Watch the group chat for the state of the game.");
        SendMessage groupMessage = new SendMessage()
                .setChatId(groupChat.getId())
                .setText(String.format("@%s joined the game", user.getUserName()));

        try {
            bot.execute(personalMessage);
            bot.execute(groupMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
        }
    }

    @Override
    public void playerLeftParty(Player player) {

    }

    @Override
    public void playerAlreadyInParty(Player player) {

    }

    @Override
    public void playerNotInParty(Player player) {

    }

    @Override
    public void gameStarted(Party party, UnoGame game) {
        // TODO: Try/catch

        // Notify players about their hand
        for (Player player : game.getParty().getPlayers()) {
            String message = String.format(
                    "@%s, here are your cards\n%s",
                    player.getUser().getUserName(),
                    formatter.formatHand(player.getHand())
            );

            SendMessage sendMessage = new SendMessage()
                    .setChatId(player.getChatId())
                    .setText(message);
            try {
                bot.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        Player startingPlayer = game.getParty().getCurrent();
        Deck discardPile = game.getDiscardPile();
        SendMessage startingGameMessage = new SendMessage()
                .setChatId(groupChat.getId())
                .setText("Starting the game, the first player to play is: " + startingPlayer.getUser().getUserName());
        SendMessage cardMessage = new SendMessage()
                .setChatId(groupChat.getId())
                .setText("Top of discard pile shows: " + discardPile.getCards().peek());

        try {
            bot.execute(startingGameMessage);
            bot.execute(cardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playedInvalidCard(Player player, Card card, UnoGame game) {

    }

    @Override
    public void playedCard(Player player, Card card, UnoGame game) {

    }

    @Override
    public void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game) {

    }

    @Override
    public void drewCard(Player player, Card card, UnoGame game) {
        // Notify player
        SendMessage playerMessage = new SendMessage()
                .setChatId(player.getChatId())
                .setText(String.format("You drew a %s card", card));

        // Notify group
        SendMessage groupMessage = new SendMessage()
                .setChatId(groupChat.getId())
                .setText(String.format("%s drew a card", player.getUser().getUserName()));

        try {
            bot.execute(playerMessage);
            bot.execute(groupMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
