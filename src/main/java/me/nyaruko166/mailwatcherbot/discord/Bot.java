package me.nyaruko166.mailwatcherbot.discord;

import me.nyaruko166.mailwatcherbot.discord.listener.MessageReceiveListener;
import me.nyaruko166.mailwatcherbot.model.EmailDetail;
import me.nyaruko166.mailwatcherbot.util.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.List;

public class Bot {

    static Logger log = LogManager.getLogger(Bot.class);

    private static final String DISCORD_TOKEN = Config
            .getProperty().getDiscordToken();
    public static JDA api;

    public static void runBot() {
        log.info("Bot is starting...");
        api = JDABuilder.createLight(DISCORD_TOKEN, EnumSet.of(GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT))
                        .setActivity(Activity.of(Activity.ActivityType.CUSTOM_STATUS, "Just a random bot passing through."))
                        .addEventListeners(new MessageReceiveListener())
                        .build();
    }

    public static void pushEmail(List<EmailDetail> lstEmail) {
        lstEmail.forEach(emailDetail -> {
//            System.out.println(emailDetail.toEmbed());
            api.getGuildById(Config.getProperty().getGuildId())
               .getTextChannelById(Config.getProperty().getChannelId())
               .sendMessageEmbeds(emailDetail.toEmbed()).queue();
        });
    }

}
