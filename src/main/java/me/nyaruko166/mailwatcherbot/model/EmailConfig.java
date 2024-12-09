package me.nyaruko166.mailwatcherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class EmailConfig {

    private String email;

    private String lastHistoryId;

    public static EmailConfig configTemplate(){
        return EmailConfig.builder()
                .email(" ")
                .lastHistoryId(" ")
                .build();
    }
}
