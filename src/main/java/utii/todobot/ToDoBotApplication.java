package utii.todobot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import utii.todobot.bot.TelegramBot;

@SpringBootApplication
@EnableScheduling
public class ToDoBotApplication {
    private static final Logger log = LoggerFactory.getLogger(ToDoBotApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ToDoBotApplication.class, args);
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
