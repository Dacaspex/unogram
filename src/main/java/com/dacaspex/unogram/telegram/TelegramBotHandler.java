package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.common.GameControllerStorage;
import com.dacaspex.unogram.common.PlayerStorage;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.telegram.command.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
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

    private final TelegramSender sender;

    private final HelpCommand helpCommand;
    private final CreateCommand createCommand;
    private final JoinCommand joinCommand;
    private final LeaveCommand leaveCommand;
    private final StartCommand startCommand;
    private final PlayCommand playCommand;
    private final DrawCommand drawCommand;

    public TelegramBotHandler(
            String token,
            String botUsername,
            PlayerStorage playerStorage,
            GameControllerStorage controllerStorage
    ) {
        this.token = token;
        this.botUsername = botUsername;

        TelegramPlayerFactory playerFactory = new TelegramPlayerFactory();
        Map<Player, Chat> playerChatMap = new HashMap<>();
        this.sender = new TelegramSender(this, playerChatMap);
        this.helpCommand = new HelpCommand(sender);
        this.createCommand = new CreateCommand(
                sender,
                playerStorage,
                controllerStorage,
                playerFactory,
                playerChatMap,
                botUsername
        );
        this.joinCommand = new JoinCommand(
                sender,
                playerStorage,
                controllerStorage,
                playerFactory,
                playerChatMap
        );
        this.leaveCommand = new LeaveCommand(
                sender,
                playerStorage,
                controllerStorage,
                playerFactory,
                playerChatMap
        );
        this.startCommand = new StartCommand(
                sender,
                playerStorage,
                controllerStorage,
                playerFactory
        );
        this.playCommand = new PlayCommand(
                sender,
                playerStorage,
                controllerStorage,
                playerFactory,
                playerChatMap
        );
        this.drawCommand = new DrawCommand(
                sender,
                playerStorage,
                controllerStorage,
                playerFactory
        );
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
            // TODO
        }
    }

    private void handleCommand(Update update) throws TelegramApiException {
        if (!update.hasMessage()) {
            return;
        }

        // Delete the message send by the user
        sender.deleteMessage(update.getMessage());

        // Get the user command
        String command = update.getMessage().getText().toLowerCase();

        if (command.startsWith("/start") || command.startsWith("/help")) {
            helpCommand.execute(update);
        } else if (command.startsWith("/create")) {
            createCommand.execute(update);
        } else if (command.startsWith("/join")) {
            joinCommand.execute(update);
        } else if (command.startsWith("/leave")) {
            leaveCommand.execute(update);
        } else if (command.startsWith("/begin")) {
            startCommand.execute(update);
        } else if (command.startsWith("play") || command.startsWith("p")) {
            playCommand.execute(update);
        } else if (command.startsWith("draw") || command.startsWith("d")) {
            drawCommand.execute(update);
        } else {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "Unknown command. You can find /help to see the help."
            );
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
