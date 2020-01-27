package com.dacaspex.unogram.main;

import com.dacaspex.unogram.ai.AgentFactory;
import com.dacaspex.unogram.common.GameControllerStorage;
import com.dacaspex.unogram.common.IdGenerator;
import com.dacaspex.unogram.common.PlayerStorage;
import com.dacaspex.unogram.controller.GameControllerFactory;
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
        String botUsername = properties.getProperty("TELEGRAM_USERNAME");

        // Build components
        IdGenerator alphanumericGenerator = new IdGenerator();
        IdGenerator numericGenerator = new IdGenerator(IdGenerator.digits);

        PlayerStorage playerStorage = new PlayerStorage();
        GameControllerStorage controllerStorage = new GameControllerStorage();

        GameControllerFactory controllerFactory = new GameControllerFactory(numericGenerator, controllerStorage);

        AgentFactory agentFactory = new AgentFactory(alphanumericGenerator, playerStorage);

        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();

        TelegramBotHandler telegramBotHandler = new TelegramBotHandler(
                apiToken,
                botUsername,
                playerStorage,
                controllerStorage,
                controllerFactory,
                agentFactory
        );

        api.registerBot(telegramBotHandler);

        System.out.println("Bot started");
    }
}
