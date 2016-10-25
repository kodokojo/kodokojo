/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
import com.amazonaws.services.simpleemail.model.Message;
import io.kodokojo.service.EmailSender;
import javaslang.control.Try;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isBlank;

public class SesEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SesEmailSender.class);

    private final String from;

    private final Region region;

    public SesEmailSender(String from, Region region) {
        if (isBlank(from)) {
            throw new IllegalArgumentException("from must be defined.");
        }
        if (region == null) {
            throw new IllegalArgumentException("region must be defined.");
        }
        this.from = from;
        this.region = region;
    }

    @Override
    public void send(List<String> to, List<String> cc, List<String> ci, String object, String content, boolean htmlContent, Set<Attachment> attachments) {
        if (CollectionUtils.isEmpty(to)) {
            throw new IllegalArgumentException("to must be defined.");
        }
        if (isBlank(content)) {
            throw new IllegalArgumentException("content must be defined.");
        }

        if (attachments == null) {
            sendSimpleMail(to, cc, ci, object, content, htmlContent);
        } else {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(session);
            try {
                message.setSubject(object);
                message.setFrom(new InternetAddress(from));
                addRecipients(message, javax.mail.Message.RecipientType.TO, to);
                addRecipients(message, javax.mail.Message.RecipientType.CC, cc);
                addRecipients(message, javax.mail.Message.RecipientType.BCC, ci);

                MimeBodyPart wrap = new MimeBodyPart();
                MimeMultipart cover = new MimeMultipart("alternative");
                MimeBodyPart html = new MimeBodyPart();
                cover.addBodyPart(html);

                wrap.setContent(cover);

                MimeMultipart multiPartContent = new MimeMultipart("related");
                message.setContent(multiPartContent);
                multiPartContent.addBodyPart(wrap);

                for (Attachment attachment : attachments) {

                    MimeBodyPart attachmentPart = new MimeBodyPart();

                    attachmentPart.setHeader("Content-ID", "<" + attachment.getId() + ">");
                    attachmentPart.setFileName(attachment.getFileName());
                    attachmentPart.setContent(attachment.getContent(), attachment.mineType());

                    multiPartContent.addBodyPart(attachmentPart);
                }

                html.setContent(content, MINE_TEXT_HTML);
                PrintStream out = System.out;
                message.writeTo(out);

                // Send the email.
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                message.writeTo(outputStream);
                RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

                SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);

                AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();
                client.setRegion(region);

                client.sendRawEmail(rawEmailRequest);

            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addRecipients(MimeMessage message , javax.mail.Message.RecipientType recipientType, List<String> address) throws MessagingException {
        if (CollectionUtils.isNotEmpty(address)) {
            message.addRecipients(recipientType, convertListToAddresses(address));
        }
    }

    private static Address[] convertListToAddresses(List<String> addresses) {
        assert addresses != null : "addresses list must be defined";
        List<InternetAddress> collect = addresses.stream().map(convertToAddress()).collect(Collectors.toList());
        return collect.toArray(new Address[collect.size()]);
    }

    private static Function<String, InternetAddress> convertToAddress() {
        return address -> Try.of(() -> new InternetAddress(address)).get();
    }


    private void sendSimpleMail(List<String> to, List<String> cc, List<String> ci, String object, String content, boolean htmlContent) {

        Destination destination = new Destination().withToAddresses(to).withBccAddresses(ci).withCcAddresses(cc);
        Content subject = new Content().withData(object);
        Content bodyContent = new Content().withData(content);
        Body body;
        if (htmlContent) {
            body = new Body().withHtml(bodyContent);
        } else {
            body = new Body().withText(bodyContent);
        }
        Message message = new Message().withSubject(subject).withBody(body);
        SendEmailRequest request = new SendEmailRequest().withSource(from).withDestination(destination).withMessage(message);
        try {
            AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();
            client.setRegion(region);
            client.sendEmail(request);
        } catch (Exception e) {
            LOGGER.error("Unable to send email to {} with subject '{}'", StringUtils.join(to, ","), subject, e);
        }
    }

}
