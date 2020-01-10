package com.dacaspex.unogram.main;

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

        TelegramBotHandler telegramBotHandler = new TelegramBotHandler(apiToken, "Unogram");

        api.registerBot(telegramBotHandler);

        System.out.println("Bot started");
    }
}
