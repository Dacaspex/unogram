package com.dacaspex.unogram.main;

import com.dacaspex.unogram.game.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class GameHandler extends TelegramLongPollingBot {

    private final String token;
    private final String botUsername;

    private State state;

    private GameController gameController;
    private TelegramAnnouncer announcer;

    public GameHandler(String token, String botUsername) {
        this.token = token;
        this.botUsername = botUsername;
        this.state = State.IDLE;
        this.gameController = new GameController(new ConsoleAnnouncer());
        this.announcer = new TelegramAnnouncer(this);
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

            announcer.setGroupChat(update.getMessage().getChat());

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
                    Player player = new Player(user, update.getMessage().getChatId());

                    boolean joined = gameController.join(player);
                    if (joined) {
                        announcer.addUserMapping(user, player, update.getMessage().getChat());
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

            if (unoGame.getParty().getCurrent() != player) {
                SendMessage noTurnMessage = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("It is not your turn.");
                execute(noTurnMessage);

                return;
            }

            // Can we play the card?
            Card card = player.getHand().getCards().get(cardIndex);
            if (unoGame.canPlay(player, card)) {
                if (card.getSuit() == Suit.WILD) {
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
                                .setChatId(player.getChatId())
                                .setText("Invalid suit, must be either 'red', 'yellow', 'green' or 'blue'");
                        execute(invalidTypeMessage);

                        return;
                    }

                    // TODO: Notify group about chosen suit

                    unoGame.playWild(player, card, chosenSuit);
                } else {
                    unoGame.play(player, card);
                }

                // TODO: 2 player rules and messages
                // Next player has been assigned
                switch (card.getType()) {
                    case SKIP:
                        Player skippedPlayer = unoGame.getParty().getPrevious();
                        SendMessage skipMessage = new SendMessage()
                                .setChatId(skippedPlayer.getChatId())
                                .setText("You were skipped because of a skip card");
                        execute(skipMessage);
                        break;
                    case REVERSE:
                        Player reversedPlayer = unoGame.getParty().getPrevious(2);
                        SendMessage reverseMessage = new SendMessage()
                                .setChatId(reversedPlayer.getChatId())
                                .setText("You were skipped because of a reverse card");
                        execute(reverseMessage);
                        break;
                    case DRAW_2:
                        Player nextPlayer = unoGame.getParty().getCurrent();
                        SendMessage drawMessage = new SendMessage()
                                .setChatId(nextPlayer.getChatId())
                                .setText("You were forced to draw 2 cards");
                        execute(drawMessage);
                        break;
                    case WILD_CHOOSE:
                        break;
                    case WILD_DRAW:
                        break;
                }

                // Notify group about card that has been played
                Player nextPlayer = unoGame.getParty().getCurrent();
                SendMessage groupMessage = new SendMessage()
                        .setChatId(groupChat.getId())
                        .setText(
                                String.format(
                                        "@%s played a %s card. Next player is %s",
                                        player.getUser().getUserName(),
                                        card,
                                        nextPlayer.getUser().getUserName()
                                )
                        );
                execute(groupMessage);

                // Notify next player that it is his/her turn
                Hand nextPlayerHand = nextPlayer.getHand();
                SendMessage notifyNextPlayerMessage = new SendMessage()
                        .setChatId(nextPlayer.getChatId())
                        .setText("It is your turn to play a card. This is your hand:\n" + formatter.formatHand(nextPlayerHand));
                execute(notifyNextPlayerMessage);
            } else {
                SendMessage invalidCardMessage = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("That card cannot be played");
                execute(invalidCardMessage);
            }
        } else if (update.getMessage().getText().startsWith("/draw")) {
            Player player = gameController.getParty().getCurrent();
            gameController.draw(player);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
