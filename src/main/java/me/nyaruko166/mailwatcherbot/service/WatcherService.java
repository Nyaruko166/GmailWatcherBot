package me.nyaruko166.mailwatcherbot.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.nyaruko166.mailwatcherbot.model.EmailDetail;
import me.nyaruko166.mailwatcherbot.util.GeneralHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WatcherService {

    private static final Logger log = LogManager.getLogger(WatcherService.class);
    @Autowired
    private GmailService gmailService;
    private final Gson gson = new Gson();

    public void pushNotificationHandler(String pushResponse) {
        JsonObject jsonMessage = gson.fromJson(pushResponse, JsonObject.class)
                .getAsJsonObject("message");

        JsonObject jsonData =
                gson.fromJson(GeneralHelper.base64UrlDecoder(jsonMessage.get("data").getAsString()), JsonObject.class);

        log.info("Email: {} | HistoryId: {}",
                jsonData.get("emailAddress").getAsString(), jsonData.get("historyId").getAsString());

        List<EmailDetail> lstEmail = gmailService.getEmails(jsonData);
        if (lstEmail != null) {
            lstEmail.forEach(System.out::println);
        }
    }
}
