/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.commons.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isBlank;

public class SmtpEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final String username;

    private final String password;

    private final Address from;

    private final Properties properties;

    public SmtpEmailSender(String host, int port,String username, String password, String fromAddr) {
        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined.");
        }
        if (isBlank(username)) {
            throw new IllegalArgumentException("username must be defined.");
        }
        if (isBlank(password)) {
            throw new IllegalArgumentException("password must be defined.");
        }
        if (isBlank(fromAddr)) {
            throw new IllegalArgumentException("fromAddr must be defined.");
        }
        this.username = username;
        this.password = password;

        this.properties = new Properties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", Integer.toString(port));
        properties.setProperty("mail.smtp.port", Integer.toString(port));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.starttls.required", "true");
        try {
            from = new InternetAddress(fromAddr);
        } catch (AddressException e) {
            throw new IllegalArgumentException("Unable to create an address from '" + fromAddr + "'.", e);
        }
    }

    @Override
    public void send(List<String> to, List<String> cc, List<String> ci, String object, String content, boolean htmlContent, Set<Attachment> attachments) {
        throw new UnsupportedOperationException("Oups !");
    }

    @Override
    public void send(List<String> to, List<String> cc, List<String> ci, String subject, String content, boolean htmlContent) {
        if (CollectionUtils.isEmpty(to)) {
            throw new IllegalArgumentException("to must be defined.");
        }
        if (isBlank(content)) {
            throw new IllegalArgumentException("content must be defined.");
        }
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        Message message = new MimeMessage(session);
        try {
            message.setFrom(from);
            message.setSubject(subject);
            InternetAddress[] toInternetAddress = convertToInternetAddress(to);
            message.setRecipients(Message.RecipientType.TO, toInternetAddress);
            if (CollectionUtils.isNotEmpty(cc)) {
                InternetAddress[] ccInternetAddress = convertToInternetAddress(cc);
                message.setRecipients(Message.RecipientType.CC, ccInternetAddress);
            }
            if (CollectionUtils.isNotEmpty(ci)) {
                InternetAddress[] ciInternetAddress = convertToInternetAddress(ci);
                message.setRecipients(Message.RecipientType.BCC, ciInternetAddress);
            }
            if (htmlContent) {
                message.setContent(content, "text/html");
            } else {
                message.setText(content);
            }
            message.setHeader("X-Mailer", "Kodo Kojo mailer");
            message.setSentDate(new Date());
            Transport.send(message);

        } catch (MessagingException e) {
           LOGGER.error("Unable to send email to {} with subject '{}'", StringUtils.join(to, ","), subject, e);
        }
    }

    private static InternetAddress[] convertToInternetAddress(List<String> input) {
        List<InternetAddress> res = input.stream().map(addr -> {
            try {
                return new InternetAddress(addr);
            } catch (AddressException e) {
                LOGGER.error("Ignoring following address to send mail.'{}'",addr);
                return null;
            }
        }).filter(addr -> addr != null).collect(Collectors.toList());
        return res.toArray(new InternetAddress[res.size()]);
    }

}
