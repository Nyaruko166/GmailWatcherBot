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
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
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
    private static final List<String> SCOPES = List.of(GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_METADATA, GmailScopes.GMAIL_LABELS);
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
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));

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
        GmailService gmailService = new GmailService();
        gmailService.autoWatcher();
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
                Scanner fileReader = new Scanner(emailFile);
                while (fileReader.hasNextLine()) {
                    String email = fileReader.nextLine();
                    log.info(email);
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void startWatching(String email) {
        try {
            Gmail userService = getGmailService("user");
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

//    public static void main(String[] args) throws IOException {
//        GmailService gmailService = new GmailService();
//
//        Gmail service = gmailService.getGmailService("user");
//        Gmail service1 = gmailService.getGmailService("user1");
//        ListMessagesResponse listResponse = service.users()
//                .messages().list("me")
//                .setLabelIds(Collections.singletonList("UNREAD"))
//                .setMaxResults(Long.valueOf("10"))
//                .execute();
//
//        List<Message> listMessages = listResponse.getMessages();
//
//        ListMessagesResponse listResponse1 = service1.users()
//                .messages().list("me")
//                .setLabelIds(Collections.singletonList("UNREAD"))
//                .setMaxResults(Long.valueOf("10"))
//                .execute();
//
//        List<Message> listMessages1 = listResponse1.getMessages();
//
//        // Fetch email subject from each message
//        for (Message message : listMessages) {
//            Message fullMessage = service.users().messages().get("me", message.getId()).setFormat("metadata").execute();
//            fullMessage.getPayload().getHeaders().stream()
//                    .filter(header -> "Subject".equalsIgnoreCase(header.getName()))
//                    .findFirst()
//                    .ifPresent(header -> System.out.println("Email Subject: " + header.getValue()));
//        }
//
//        // Fetch email subject from each message
//        System.out.println("User1:");
//        for (Message message : listMessages1) {
//            Message fullMessage = service1.users().messages().get("me", message.getId()).setFormat("metadata").execute();
//            fullMessage.getPayload().getHeaders().stream()
//                    .filter(header -> "Subject".equalsIgnoreCase(header.getName()))
//                    .findFirst()
//                    .ifPresent(header -> System.out.println("Email Subject: " + header.getValue()));
//        }

//        ListLabelsResponse listResponse = service.users().labels().list("me").execute();
//
//        List<Label> labels = listResponse.getLabels();
//        if (labels.isEmpty()) {
//            System.out.println("No labels found.");
//        } else {
//            System.out.println("Labels:");
//            for (Label label : labels) {
//                System.out.printf("- %s\n", label.getName());
//            }
//        }
//    }
}
