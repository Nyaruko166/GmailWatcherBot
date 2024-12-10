package me.nyaruko166.mailwatcherbot.util;

import me.nyaruko166.mailwatcherbot.model.EmailDetail;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class GeneralHelper {

    public static String dateConverter(String dateStr) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy (EEE)");
//        System.out.println(dateStr);
//        System.out.println(dateStr.replaceAll("\\s\\+\\d{4}$", ""));
        return outputFormatter.format(inputFormatter.parse(dateStr.replaceAll("\\s\\+\\d{4}$", "")));
    }

    public static String base64UrlDecoder(String base64String) {
        return new String(Base64.getUrlDecoder().decode(base64String));
    }
}
