package me.nyaruko166.mailwatcherbot.model;

import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.nyaruko166.mailwatcherbot.util.GeneralHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class EmailDetail {

    private String from;

    private String date;

    private String subject;

    private String to;

//    private String body;

    private String bodyPart;

    public static EmailDetail toEmailDetail(MessagePart messagePart) {
        EmailDetail emailDetail = new EmailDetail();

        List<MessagePartHeader> lstHeader = messagePart.getHeaders();
        for (MessagePartHeader header : lstHeader) {
            if (header.getName().equals("From")) {
                emailDetail.setFrom(header.getValue());
            }
            if (header.getName().equals("Date")) {
//                System.out.println(header.getValue());
//                emailDetail.setDate(GeneralHelper.dateConverter(header.getValue()));
                emailDetail.setDate(header.getValue());
            }
            if (header.getName().equals("Subject")) {
                emailDetail.setSubject(header.getValue());
            }
            if (header.getName().equals("To")) {
                emailDetail.setTo(header.getValue());
            }
        }

//        String body = messagePart.getBody() != null && messagePart.getBody().getData() != null
//                ? GeneralHelper.base64UrlDecoder(messagePart.getBody().getData())
//                : null;
//        emailDetail.setBody(body);

        String bodyPart = messagePart.getParts() != null
                ? GeneralHelper.base64UrlDecoder(messagePart.getParts().getFirst().getBody().getData())
                : null;
        emailDetail.setBodyPart(bodyPart);

        return emailDetail;
    }

    public MessageEmbed toEmbed() {
        return new EmbedBuilder()
                .setColor(Color.GREEN)
//                .setAuthor(botInfo.getName(), "https://www.facebook.com/nyaruko166", botInfo.getAvatarUrl())
                .setTitle("Tiêu đề: %s".formatted(this.subject))
                .addField("Người gửi: ", this.from, false)
                .addField("Người nhận: ", this.to, false)
                .addField("Nội dung: ", this.bodyPart == null ? "Thư rỗng?!" : this.bodyPart, false)
                .setFooter("Ngày nhận: %s".formatted(this.date.replaceAll("\\s\\+\\d{4}$", "")))
                .build();
    }
}
