package com.dacaspex.unogram.main;

import com.dacaspex.unogram.telegram.TelegramBotHandler;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Application {
    public static void main(String[] args) throws IOException, TelegramApiRequestException {
        System.out.println("Starting bot...");

        // Load config
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("config.properties")));

        String apiToken = properties.getProperty("TELEGRAM_API_TOKEN");

        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();

        TelegramBotHandler telegramBotHandler = new TelegramBotHandler(
                apiToken,
                "uno_dev_bot"
        );

        api.registerBot(telegramBotHandler);

        System.out.println("Bot started");
    }
}
