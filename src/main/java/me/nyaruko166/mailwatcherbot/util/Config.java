package me.nyaruko166.mailwatcherbot.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.nyaruko166.mailwatcherbot.model.EmailConfig;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class Config {

    private static final Logger log = LogManager.getLogger(Config.class);
    private static final Gson gson = new Gson();
    private static final File configFile = new File("./libs/config.json");

    // Eager Singleton instance
    private static final Config instance = new Config();
    private static List<EmailConfig> lstEmailConfig;

    // Private constructor
    private Config() {
        if (!configFile.exists()) {
            try {
                log.info("Creating config file...");
                JsonArray tempJsonArr = new JsonArray();
                String tempJson = gson.toJson(EmailConfig.configTemplate());
                System.out.print("How many account do you want to watch? ");
                int n = Integer.parseInt(new Scanner(System.in).nextLine());
                for (int i = 0; i < n; i++) {
                    tempJsonArr.add(gson.fromJson(tempJson, JsonElement.class));
                }
                FileUtils.writeStringToFile(
                        configFile, tempJsonArr.toString(), StandardCharsets.UTF_8);
                log.info("Config file created successfully.");
                log.info("Please navigate to ./libs to setup app config");
                System.exit(0);
            } catch (IOException e) {
                log.error("Failed to create config file", e);
            }
        }
        loadConfig();
    }

    // Method to load the configuration
    private void loadConfig() {
        try {
            lstEmailConfig = gson.fromJson(new FileReader(configFile), new TypeToken<List<EmailConfig>>() {
            }.getType());
        } catch (FileNotFoundException e) {
            log.error("Error when loading config file", e);
        }
    }

    // Method to update the configuration file
    public static void updateConfig() {
        try {
            FileUtils.writeStringToFile(configFile, gson.toJsonTree(lstEmailConfig).getAsJsonArray().toString(), "UTF-8");
            log.info("Configuration updated successfully.");
        } catch (IOException e) {
            log.error("Failed to update config file", e);
        }
    }

    // Combined method to access the AppConfig properties
    public static List<EmailConfig> getProperty() {
        return lstEmailConfig;
    }

    public static String updateHistoryIdByEmail(String email, String newHistoryId) {
        String oldHistoryId = null;
        for (EmailConfig emailConfig : lstEmailConfig) {
            if (emailConfig.getEmail().equalsIgnoreCase(email)) {
                oldHistoryId = emailConfig.getLastHistoryId();
                emailConfig.setLastHistoryId(newHistoryId);
            }
        }
        updateConfig();
        return oldHistoryId;
    }

    public static void setLstEmailConfig(List<EmailConfig> lstEmailConfig) {
        Config.lstEmailConfig = lstEmailConfig;
    }
}