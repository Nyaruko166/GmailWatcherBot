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
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

@Service
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
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, String email) throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").setApprovalPrompt("force").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(6969).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(email);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doShesshOnStartup() {
        File libFolder = new File("./libs");
        if (!libFolder.exists()) {
            libFolder.mkdir();
        }
        autoWatcher();
    }

    public Gmail getGmailService(String email) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = getCredentials(HTTP_TRANSPORT, email);
            return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Something when wrong when getting Gmail Service?!");
            log.error(e);
            return null;
        }
    }

    //Todo: Set up cron job renew watching every day
    public void autoWatcher() {
        try {
            File clientSecrets = new File("./libs/StoredCredential");
            File emailFile = new File("./libs/ListEmail.txt");
            if (!clientSecrets.exists()) {
                if (!emailFile.exists()) {
                    emailFile.createNewFile();
                    log.info("ListEmail.txt created successfully.");
                    log.info("Please navigate to ./libs to input your email");
                    System.exit(0);
                }
                if (emailFile.length() == 0) {
                    log.info("ListEmail.txt is empty.");
                    log.info("Please navigate to ./libs to input your email");
                    System.exit(0);
                }
            }
            Scanner fileReader = new Scanner(emailFile);
            while (fileReader.hasNextLine()) {
                String email = fileReader.nextLine();
                startWatching(email);
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void startWatching(String email) {
        try {
            Gmail userService = getGmailService(email);
            WatchRequest request = new WatchRequest();
            request.setLabelIds(Collections.singletonList("INBOX"));
            request.setTopicName("projects/gg-drive-api-439408/topics/gmail_notification");
            request.setLabelFilterBehavior("INCLUDE");
            WatchResponse watchResponse = userService.users().watch("me", request).execute();
            System.out.println(watchResponse);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public static void main(String[] args) throws IOException {

        GmailService gmailService = new GmailService();

        Gmail service = gmailService.getGmailService("12312312312312");

        // History ID from the Pub/Sub notification
        String historyId = "6154894";

        // Retrieve history list from Gmail
        ListHistoryResponse historyResponse = service.users().history()
                .list("me")
                .setStartHistoryId(new BigInteger(historyId))
                .execute();

        // Process the history
        List<History> histories = historyResponse.getHistory();
        if (histories == null || histories.isEmpty()) {
            System.out.println("No history found.");
            return;
        }

        for (History history : histories) {
            List<Message> lstMess = history.getMessages();
            if (lstMess != null) {
                for (Message newMess : lstMess) {
                    String messageId = newMess.getId();

                    // Fetch the full message details
                    Message message = service.users().messages().get("me", messageId).setFormat("full").execute();

                    // Print all message details
                    System.out.println("Message ID: " + message.getId());
                    System.out.println("Thread ID: " + message.getThreadId());
                    System.out.println("Labels: " + message.getLabelIds());

                    // Print headers
//                    if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
//                        message.getPayload().getHeaders().forEach(header ->
//                                System.out.println(header.getName() + ": " + header.getValue()));
//                    }

                    // Print email body (base64-decoded)
                    if (message.getPayload() != null && message.getPayload().getBody() != null) {
                        String bodyData = message.getPayload().getBody().getData();
                        if (bodyData != null) {
                            String body = new String(Base64.getUrlDecoder().decode(bodyData));
                            System.out.println("Email Body: " + body);
                        } else {
                            System.out.println("No body found for this message.");
                        }
                    }

                    System.out.println(message.getPayload());
//                    System.out.println(message.getPayload().get);

                }
            }
        }
    }
}
