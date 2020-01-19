package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.game.Player;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TelegramSender {
    private final int DEFAULT_DELAY = 4000;

    private final TelegramBotHandler bot;
    private final Map<Player, Chat> playerChatMap;
    private final Timer timer;

    public TelegramSender(TelegramBotHandler bot, Map<Player, Chat> playerChatMap) {
        this.bot = bot;
        this.playerChatMap = playerChatMap;
        this.timer = new Timer();
    }

    public Message sendMessage(Player player, String message) {
        Chat chat = playerChatMap.getOrDefault(player, null);

        if (chat == null) {
            throw new IllegalStateException("Chat cannot be null");
        }

        return sendMessage(chat, message);
    }

    public Message sendMessage(Chat chat, String message) {
        long chatId = chat.getId();
        try {
            return bot.execute(new SendMessage(chatId, message).setParseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
            return null;
        }
    }

    public void sendAndDeleteAfterDelay(Player player, String message) {
        deleteMessageAfterDelay(sendMessage(player, message));
    }

    public void sendAndDeleteAfterDelay(Player player, String message, int delay) {
        deleteMessageAfterDelay(sendMessage(player, message), delay);
    }

    public void sendAndDeleteAfterDelay(Chat chat, String message) {
        deleteMessageAfterDelay(sendMessage(chat, message));
    }

    public void editMessage(Message message, String text) {
        try {
            bot.execute(
                    new EditMessageText()
                            .setChatId(message.getChatId())
                            .setMessageId(message.getMessageId())
                            .setText(text)
                            .setParseMode(ParseMode.HTML)
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
        }
    }

    public void deleteMessage(Message message) {
        try {
            bot.execute(new DeleteMessage(message.getChatId(), message.getMessageId()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
        }
    }

    public void deleteMessageAfterDelay(Message message) {
        deleteMessageAfterDelay(message, DEFAULT_DELAY);
    }

    public void deleteMessageAfterDelay(Message message, int delay) {
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        deleteMessage(message);
                    }
                },
                delay
        );
    }
}
