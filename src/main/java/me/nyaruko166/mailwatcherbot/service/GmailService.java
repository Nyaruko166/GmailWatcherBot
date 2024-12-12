package me.nyaruko166.mailwatcherbot.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import com.google.gson.JsonObject;
import me.nyaruko166.mailwatcherbot.model.EmailConfig;
import me.nyaruko166.mailwatcherbot.model.EmailDetail;
import me.nyaruko166.mailwatcherbot.util.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@EnableScheduling
public class GmailService {

    private static final String APPLICATION_NAME = "Gmail API Jav";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "./libs";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_MODIFY);
    //Change according to your credentials secret file
    private static final String CREDENTIALS_FILE_PATH = "./libs/gmail_cred.json";
    private static final Logger log = LogManager.getLogger(GmailService.class);

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, String email) throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                        .setAccessType("offline").setApprovalPrompt("force").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(6969).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(email);
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void runAtMidnight(){
        autoWatcher();
        log.info("Auto renew watcher at midnight complete successfully!");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doShesshOnStartup() {
        File libFolder = new File("./libs");
        if (!libFolder.exists()) {
            libFolder.mkdir();
        }

        log.info("Start up scripts completed...");
    }

    public Gmail getGmailService(String email) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = getCredentials(HTTP_TRANSPORT, email);
            return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Something went wrong when getting Gmail Service?!");
            log.error(e);
            return null;
        }
    }

    public void autoWatcher() {
        List<EmailConfig> lstConfig = Config.getProperty().getListEmail();
        lstConfig.forEach(emailConfig -> {
            if (emailConfig.getEmail().isBlank()) {
                log.warn("Email is blank, skipping this empty mailbox. Please recheck the config later");
            } else {
                emailConfig.setLastHistoryId(startWatching(emailConfig.getEmail()));
            }
        });
        Config.getProperty().setListEmail(lstConfig);
        Config.updateConfig();
    }

    public String startWatching(String email) {
        try {
            Gmail userService = getGmailService(email);
            WatchRequest request = new WatchRequest();
            request.setLabelIds(Collections.singletonList("UNREAD"));
            request.setTopicName("projects/%s/topics/%s"
                    .formatted(Config.getProperty().getProjectId(), Config.getProperty().getTopicName()));
            request.setLabelFilterBehavior("INCLUDE");
            WatchResponse watchResponse = userService.users().watch("me", request).execute();
            log.info("Start watching {}'s mailbox successfully!", email);
            log.info("History ID: {}", watchResponse.getHistoryId());
            return watchResponse.getHistoryId().toString();
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }

    public List<EmailDetail> getEmails(JsonObject jsonData) {
        try {
            List<EmailDetail> lstEmail = new ArrayList<>();
            String email = jsonData.get("emailAddress").getAsString();
            String newHistoryId = jsonData.get("historyId").getAsString();
            Gmail service = getGmailService(email);

            String oldHistoryId = Config.updateHistoryIdByEmail(email, newHistoryId);

            // Retrieve history list from Gmail
            ListHistoryResponse historyResponse = service.users().history()
                    .list("me")
                    .setStartHistoryId(new BigInteger(oldHistoryId))
                    .execute();

            // Process the history
            List<History> histories = historyResponse.getHistory();
            if (histories == null || histories.isEmpty()) {
                log.info("No history found.");
                return null;
            }

            for (History history : histories) {
                List<HistoryMessageAdded> lstAddedMess = history.getMessagesAdded();
                if (lstAddedMess != null) {
                    for (HistoryMessageAdded addedMess : lstAddedMess) {
                        String messageId = addedMess.getMessage().getId();

                        // Fetch the full message details
                        Message message = service.users()
                                .messages().get("me", messageId)
                                .setFormat("full").execute();

                        EmailDetail emailDetail = EmailDetail.toEmailDetail(message.getPayload());

                        lstEmail.add(emailDetail);
                    }
                }
            }
            return lstEmail;
        } catch (IOException e) {
            log.error("Error when getting emails!");
            log.error(e);
            return null;
        }
    }
}
