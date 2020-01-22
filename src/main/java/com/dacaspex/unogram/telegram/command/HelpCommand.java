package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.common.Emoji;
import com.dacaspex.unogram.telegram.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.Update;

public class HelpCommand {

    private final TelegramSender sender;

    public HelpCommand(TelegramSender sender) {
        this.sender = sender;
    }

    public void execute(Update update) {
        sender.sendMessage(
                update.getMessage().getChat(),
                String.format(
                        "" +
                                "%s Uno Telegram Bot information\n" +
                                "\n" +
                                "This is a Telegram Bot to play a game of Uno with your friends.\n" +
                                "\n" +
                                "%s Commands\n" +
                                "- <code>/help</code> to show this help menu.\n" +
                                "- <code>/create</code> to create a new game with default options. You will become " +
                                "the host of this game.\n" +
                                "- <code>/begin</code> as a host, use this to start the game.\n" +
                                "- <code>/join &lt;game id&gt</code>; use this as a player to join a game.\n" +
                                "- <code>/addagent</code> as a host, use this to add AI players to the game.\n" +
                                "- <code>/removeagent</code> as a host, use this to remove AI players from" +
                                "the game.\n" +
                                "\n" +
                                "%s Actions in-game\n" +
                                "- <code>play &lt;number&gt;</code> to play a card where the number equals the " +
                                "number in your hand. The shorthand <code>p</code> can also be used.\n" +
                                "- <code>play &lt;number&gt; &lt;suit&gt;</code> similar as above, but for a " +
                                "wild card you must specify the suit you want to change to, which is " +
                                "either red, yellow green or blue.\n" +
                                "- <code>draw</code> to draw a card from the pile. There is also a shorthand " +
                                "<code>d</code>.",
                        Emoji.INFORMATION,
                        Emoji.PLAY_BUTTON,
                        Emoji.JOYSTICK
                )
        );

//        sender.sendMessage(
//                update.getMessage().getChat(),
//                "" +
//                        "-- Uno Telegram Bot - Help --\n" +
//                        "\n" +
//                        "'help' for this help menu\n" +
//                        "'create' to create a new game\n" +
//                        "'start' to start a game\n" +
//                        "'join' &lt;game id &gt; to join a game\n" +
//                        "'play &lt;number&gt;' to play a normal card\n" +
//                        "'play &lt;number&gt; &lt;suit&gt;' to play a wild card\n"
//        );
    }
}
