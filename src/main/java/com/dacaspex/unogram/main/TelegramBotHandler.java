package com.dacaspex.unogram.main;

import com.dacaspex.unogram.game.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

/**
 * Telegram bot. Responsible for mapping the incoming updates from Telegram to
 * actions in the game controller.
 */
public class TelegramBotHandler extends TelegramLongPollingBot {

    private final String token;
    private final String botUsername;

    private State state;

    private GameController gameController;
    private TelegramAnnouncer telegramAnnouncer;
    private Map<User, Player> playerMap;

    public TelegramBotHandler(String token, String botUsername) {
        this.token = token;
        this.botUsername = botUsername;
        this.state = State.IDLE;
        this.telegramAnnouncer = new TelegramAnnouncer(this);
        this.gameController = new GameController(
                new CompoundAnnouncer(telegramAnnouncer, new ConsoleAnnouncer())
        );
        this.playerMap = new HashMap<>();
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            switch (state) {
                case IDLE:
                    handleIdle(update);
                    break;
                case AWAITING_PLAYERS:
                    handleAwaitingPlayers(update);
                    break;
                case IN_GAME:
                    handleInGame(update);
                    break;
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleIdle(Update update) throws TelegramApiException {
        if (!update.hasMessage()) {
            return;
        }

        if (!update.getMessage().isGroupMessage()) {
            return;
        }

        if (update.getMessage().getText().equals("/start")) {
            state = State.AWAITING_PLAYERS;

            telegramAnnouncer.setGroupChat(update.getMessage().getChat());

            SendMessage sendMessage = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Preparing a fresh game of Uno! Send me a personal message using the game code '1234'.");

            execute(sendMessage);
        }
    }

    private void handleAwaitingPlayers(Update update) throws TelegramApiException {
        if (!update.hasMessage()) {
            return;
        }

        if (update.getMessage().isUserMessage()) {
            if (update.getMessage().getText().startsWith("/join")) {
                // Get game id
                String gameId = update.getMessage().getText().split(" ", 2)[1];
                if (gameId.equals("1234")) {
                    User user = update.getMessage().getFrom();
                    Player player = playerMap.getOrDefault(user, new Player(update.getMessage().getFrom().getUserName()));

                    telegramAnnouncer.addUserMapping(user, player, update.getMessage().getChat());
                    playerMap.put(user, player);

                    boolean joined = gameController.join(player);
                    if (!joined) {
                        // TODO: Remove user if not joined
                    }
                } else {
                    SendMessage sendMessage = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Incorrect game id, please give me a correct game id using /join game_id");

                    execute(sendMessage);
                }
            }
        } else if (update.getMessage().isGroupMessage()) {
            if (update.getMessage().getText().startsWith("/begin")) {
                // TODO: Check enough players
                state = State.IN_GAME;

                gameController.start();
            }
        }
    }

    private void handleInGame(Update update) throws TelegramApiException {
        if (!update.hasMessage()) {
            return;
        }

        if (!update.getMessage().isUserMessage()) {
            return;
        }

        if (update.getMessage().getText().startsWith("/play")) {
            // TODO: Format error
            int cardIndex = Integer.parseInt(update.getMessage().getText().split(" ")[1]);
            Player player = playerMap.get(update.getMessage().getFrom());
            Card card = player.getHand().getCards().get(cardIndex);

            if (card.getSuit() == Suit.WILD) {
                // Get chosen colour
                // TODO: Format error
                String[] parts = update.getMessage().getText().split(" ");
                String text;
                if (parts.length < 3) {
                    text = "invalid";
                } else {
                    text = update.getMessage().getText().split(" ", 3)[2];
                }

                Suit chosenSuit;
                if (text.equals("red")) {
                    chosenSuit = Suit.RED;
                } else if (text.equals("yellow")) {
                    chosenSuit = Suit.YELLOW;
                } else if (text.equals("green")) {
                    chosenSuit = Suit.GREEN;
                } else if (text.equals("blue")) {
                    chosenSuit = Suit.BLUE;
                } else {
                    SendMessage invalidTypeMessage = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Invalid suit, must be either 'red', 'yellow', 'green' or 'blue'");
                    execute(invalidTypeMessage);

                    return;
                }

                gameController.play(player, card, chosenSuit);
            } else {
                gameController.play(player, card);
            }
        } else if (update.getMessage().getText().startsWith("/draw")) {
            // Get the player that the user of the message maps to
            Player player = playerMap.get(update.getMessage().getFrom());
            gameController.draw(player);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
