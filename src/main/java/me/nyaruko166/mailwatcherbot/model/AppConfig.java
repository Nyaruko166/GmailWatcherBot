package me.nyaruko166.mailwatcherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class AppConfig {

    private List<EmailConfig> listEmail;

    private String projectId;

    private String topicName;

    private String discordToken;

    private String guildId;

    private String channelId;

}
