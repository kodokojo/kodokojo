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
package io.kodokojo.service.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
import io.kodokojo.service.EmailSender;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
    public void send(List<String> to, List<String> cc, List<String> ci, String object, String content, boolean htmlContent) {
        if (CollectionUtils.isEmpty(to)) {
            throw new IllegalArgumentException("to must be defined.");
        }
        if (isBlank(content)) {
            throw new IllegalArgumentException("content must be defined.");
        }
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
