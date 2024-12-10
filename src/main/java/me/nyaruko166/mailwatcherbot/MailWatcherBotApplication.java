package me.nyaruko166.mailwatcherbot;

import me.nyaruko166.mailwatcherbot.discord.Bot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MailWatcherBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailWatcherBotApplication.class, args);
        Bot.runBot();
    }

}
