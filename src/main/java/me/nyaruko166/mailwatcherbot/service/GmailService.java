package me.nyaruko166.mailwatcherbot.service;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import java.util.Collections;
import java.util.List;

public class GmailService {

    private static final String APPLICATION_NAME = "Gmail API Jav";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "./libs";

    private static final List<String> SCOPES = Collections.singletonList();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
}
