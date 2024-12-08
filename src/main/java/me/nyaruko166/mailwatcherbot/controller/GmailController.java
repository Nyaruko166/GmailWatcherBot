package me.nyaruko166.mailwatcherbot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gmail")
public class GmailController {

    @PostMapping("/watcher")
    public ResponseEntity<?> getPubSubPost(@RequestBody String response) {
        System.out.println(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Test ngu nhu cho");
    }

}
