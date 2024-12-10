package me.nyaruko166.mailwatcherbot.discord.listener;

import me.nyaruko166.mailwatcherbot.util.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageReceiveListener extends ListenerAdapter {

    private final String PREFIX = "!";

    private final Logger log = LogManager.getLogger(MessageReceiveListener.class);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return; // Who care about bot? Bruh wanna make a recursive bot xD
        String message = event.getMessage().getContentRaw();
        ;
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)

        if (message.startsWith(PREFIX)) {
            String command = message.substring(PREFIX.length()).toLowerCase();
            log.info("Author: {} | Command: {}", event.getAuthor().getName(), command);
            switch (command) {
                case "ping" -> event.getChannel().sendMessage("Pong!").queue();
                case "setup log" -> {
                    if (!hasAdminPermission(event)) return;
                    String guildID = event.getGuild().getId();
                    String channelID = event.getChannel().getId();
                    event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setAuthor(event.getAuthor().getName(),
                                    "https://www.facebook.com/nyaruko166", event.getAuthor().getAvatarUrl())
                            .setTitle("Using this channel for new email notification.")
                            .setDescription("Day la description? Cho cc gi?")
                            .setFooter(String.format("%s - %s",
                                            event.getJDA().getSelfUser().getName(),
                                            LocalDateTime.now().format(timeFormatter)),
                                    event.getJDA().getSelfUser().getAvatarUrl())
                            .setColor(Color.GREEN).build()).queue();
                    event.getMessage().delete().queue();
                    Config.getProperty().setGuildId(guildID);
                    Config.getProperty().setChannelId(channelID);
                    Config.updateConfig();
                }
                case "help" -> sendHelpMessage(event);
                default ->
                        event.getChannel().sendMessage("Unknown command! Use `!help` for a list of commands.").queue();
            }
        }
    }

    private void sendHelpMessage(MessageReceivedEvent event) {
        EmbedBuilder helpEmbed = new EmbedBuilder();
        helpEmbed.setTitle("Bot Commands");
        helpEmbed.setColor(Color.BLUE);
        helpEmbed.setDescription("Here are the available commands:");
        helpEmbed.addField("!ping", "Replies with 'Pong!'", false);
//        helpEmbed.addField("!mc", "Check minecraft server.", false);
        helpEmbed.addField("!setup log", "Sech.", false);
        helpEmbed.addField("!help", "Displays this help message.", false);

        event.getChannel().sendMessageEmbeds(helpEmbed.build()).queue();
    }

    private boolean hasAdminPermission(MessageReceivedEvent event) {
        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        } else {
            event.getChannel().sendMessage("You don't have permission to use this command.").queue();
            return false;
        }
    }
}
