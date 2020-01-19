package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.telegram.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.Update;

public class HelpCommand {

    private final TelegramSender sender;

    public HelpCommand(TelegramSender sender) {
        this.sender = sender;
    }

    public void execute(Update update) {
        // TODO: Reformat with emojis
        sender.sendMessage(
                update.getMessage().getChat(),
                "" +
                        "-- Uno Telegram Bot - Help --\n" +
                        "\n" +
                        "'help' for this help menu\n" +
                        "'create' to create a new game\n" +
                        "'start' to start a game\n" +
                        "'join' &lt;game id &gt; to join a game\n" +
                        "'play &lt;number&gt;' to play a normal card\n" +
                        "'play &lt;number&gt; &lt;suit&gt;' to play a wild card\n"
        );
    }
}
