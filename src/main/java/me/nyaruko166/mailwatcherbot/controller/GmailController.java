package me.nyaruko166.mailwatcherbot.controller;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import me.nyaruko166.mailwatcherbot.service.GmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Collections;

@Controller
@RequestMapping("/gmail")
public class GmailController {

    @Autowired
    private GmailService gmailService;

    @PostMapping("/watcher")
    public ResponseEntity<?> getPubSubPost(@RequestBody String response) {

        System.out.println(response);

        return ResponseEntity.ok("Nice");
    }

    @GetMapping("/watch")
    public ResponseEntity<?> startWatching() throws IOException {
        Gmail userService = gmailService.getGmailService("user");
        WatchRequest request = new WatchRequest();
        request.setLabelIds(Collections.singletonList("INBOX"));
        request.setTopicName("projects/gg-drive-api-439408/topics/gmail_notification");
        request.setLabelFilterBehavior("INCLUDE");
        WatchResponse watchResponse = userService.users().watch("me", request).execute();
        System.out.println(watchResponse);
        return ResponseEntity.ok("Sech");
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Test ngu nhu cho");
    }

}
