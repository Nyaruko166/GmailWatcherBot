package me.nyaruko166.mailwatcherbot.controller;

import me.nyaruko166.mailwatcherbot.service.WatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gmail")
public class WatcherController {

    @Autowired
    private WatcherService watcherService;

    @PostMapping("/watcher")
    public ResponseEntity<?> getPubSubPost(@RequestBody String response) {
        watcherService.pushNotificationHandler(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Test ngu nhu cho");
    }

}
