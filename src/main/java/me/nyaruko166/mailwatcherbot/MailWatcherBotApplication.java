package me.nyaruko166.mailwatcherbot;

import me.nyaruko166.mailwatcherbot.service.GmailService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class MailWatcherBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailWatcherBotApplication.class, args);
    }

}
