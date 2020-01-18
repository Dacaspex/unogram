package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.controller.GameController;
import com.dacaspex.unogram.controller.announcements.Announcer;
import com.dacaspex.unogram.controller.announcements.CompoundAnnouncer;
import com.dacaspex.unogram.controller.announcements.ConsoleAnnouncer;
import com.dacaspex.unogram.game.Card;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.game.Suit;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Telegram bot. Responsible for mapping the incoming updates from Telegram to
 * actions in the game controller.
 */
public class TelegramBotHandler extends TelegramLongPollingBot {

    private final String token;
    private final String botUsername;

    private Map<String, GameController> stringGameControllerMap;
    private Map<Player, GameController> playerGameControllerMap;
    private Map<String, TelegramAnnouncer> telegramAnnouncerMap;
    private Map<User, Player> playerMap;

    private Timer timer;

    public TelegramBotHandler(String token, String botUsername) {
        this.token = token;
        this.botUsername = botUsername;
        this.stringGameControllerMap = new HashMap<>();
        this.playerGameControllerMap = new HashMap<>();
        this.telegramAnnouncerMap = new HashMap<>();
        this.playerMap = new HashMap<>();
        this.timer = new Timer();
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            handleCommand(update);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCommand(Update update) throws TelegramApiException {
        if (!update.hasMessage()) {
            return;
        }

        String command = update.getMessage().getText();

        if (command.startsWith("create")) {
            // TODO: Check if player is not already in a game
            //   Player -> GameController.Party

            // The creator of the game is automatically the host
            User user = update.getMessage().getFrom();
            Player host = createPlayerFromUpdate(update);

            // Player map contains all active players. If player is present, then it cannot
            // be the host of the game. He/she should first leave the game
            if (playerMap.containsKey(user)) {
                sendMessage(update.getMessage().getChat(), "You are already in a game");

                return;
            }

            // Create a new game controller
            // TODO: Generate ID
            String id = "123";
            TelegramAnnouncer telegramAnnouncer = new TelegramAnnouncer(this, id);
            Announcer announcer = new CompoundAnnouncer(telegramAnnouncer, new ConsoleAnnouncer(id));
            GameController controller = new GameController(id, announcer);

            // Save mappings
            playerMap.put(user, host);
            stringGameControllerMap.put(id, controller);
            playerGameControllerMap.put(host, controller);
            telegramAnnouncerMap.put(id, telegramAnnouncer);
            telegramAnnouncer.addUserMapping(user, host, update.getMessage().getChat());

            // Create the new game
            controller.create(host);
        } else if (command.startsWith("join")) {
            User user = update.getMessage().getFrom();
            Player player = playerMap.getOrDefault(
                    user,
                    createPlayerFromUpdate(update)
            );

            // Check if the player is already present in a game, if so, he/she cannot join this game
            if (playerGameControllerMap.containsKey(player)) {
                sendMessage(
                        update.getMessage().getChat(),
                        "Cannot join the game: You are already in another game"
                );

                return;
            }

            // Check if there is a game corresponding to the id
            // TODO: Format error
            String gameId = update.getMessage().getText().split(" ", 2)[1];
            if (!stringGameControllerMap.containsKey(gameId)) {
                sendMessage(
                        update.getMessage().getChat(),
                        "There does not exist a game with that id."
                );

                return;
            }

            GameController controller = stringGameControllerMap.get(gameId);
            TelegramAnnouncer telegramAnnouncer = telegramAnnouncerMap.get(gameId);

            // Save mappings
            telegramAnnouncer.addUserMapping(user, player, update.getMessage().getChat());
            playerMap.put(user, player);
            playerGameControllerMap.put(player, controller);

            // Join player
            controller.join(player);
        } else if (command.startsWith("start")) {
            User user = update.getMessage().getFrom();
            Player player = playerMap.getOrDefault(
                    user,
                    createPlayerFromUpdate(update)
            );

            GameController controller = playerGameControllerMap.get(player);

            // Check if the player maps to a controller. If not, it has not joined a game yet
            if (controller == null) {
                sendMessage(
                        update.getMessage().getChat(),
                        "Cannot start a game because you have not joined a game yet."
                );

                return;
            }

            // Check if the player is the host. Only hosts can start the game
            if (controller.getParty().getHost() != player) {
                sendMessage(
                        update.getMessage().getChat(),
                        "Cannot start a game because you are not the host of that game."
                );

                return;
            }

            controller.start();
        } else if (command.startsWith("play")) {
            User user = update.getMessage().getFrom();
            Player player = playerMap.getOrDefault(user, createPlayerFromUpdate(update));

            // Check if the player is in a game
            if (!playerGameControllerMap.containsKey(player)) {
                sendMessage(
                        update.getMessage().getChat(),
                        "You have not joined a game."
                );

                return;
            }

            GameController controller = playerGameControllerMap.get(player);

            // TODO: Format error
            int cardIndex = Integer.parseInt(update.getMessage().getText().split(" ")[1]);
            // TODO: Out of range
            Card card = player.getHand().getCards().get(cardIndex - 1);

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

                controller.play(player, card, chosenSuit);
            } else {
                controller.play(player, card);
            }

            deleteMessageAfterDelay(update.getMessage(), 2000);

            if (controller.isFinished()) {
                controller.getParty().getPlayers().forEach(p -> {
                    playerGameControllerMap.remove(p);
                });

                // Game has finished, remove all mappings
                // TODO: Get all users and remove them
                playerMap.remove(user);
                stringGameControllerMap.remove(controller.getId());
                telegramAnnouncerMap.remove(controller.getId());
            }
        } else if (command.startsWith("draw")) {
            User user = update.getMessage().getFrom();
            Player player = playerMap.getOrDefault(user, createPlayerFromUpdate(update));

            // Check if the player is in a game
            if (!playerGameControllerMap.containsKey(player)) {
                sendMessage(
                        update.getMessage().getChat(),
                        "You have not joined a game."
                );

                return;
            }

            GameController controller = playerGameControllerMap.get(player);
            controller.draw(player);

            deleteMessageAfterDelay(update.getMessage(), 2000);
        }
    }

    private void sendMessage(Chat chat, String message) throws TelegramApiException {
        execute(
                new SendMessage()
                        .setChatId(chat.getId())
                        .setText(message)
        );
    }

    private Player createPlayerFromUpdate(Update update) {
        return new Player(String.format("@%s", update.getMessage().getFrom().getUserName()), "Telegram");
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private void deleteMessageAfterDelay(Message message, int delay) {
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            execute(
                                    new DeleteMessage(message.getChatId(), message.getMessageId())
                            );
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                            // TODO
                        }
                    }
                },
                delay
        );
    }
}
