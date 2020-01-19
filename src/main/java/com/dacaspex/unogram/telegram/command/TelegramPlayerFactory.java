package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.game.Player;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramPlayerFactory {
    private final static String PLATFORM_NAME = "Telegram";

    public String createId(Update update) {
        return String.format("%s/%s", PLATFORM_NAME, update.getMessage().getFrom().getId());
    }

    public Player create(Update update) {
        return new Player(
                createId(update),
                String.format(
                        "@%s",
                        update.getMessage().getFrom().getUserName()
                )
        );
    }
}
