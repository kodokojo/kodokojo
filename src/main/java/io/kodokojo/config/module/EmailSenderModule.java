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
package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.config.EmailConfig;
import io.kodokojo.service.EmailSender;
import io.kodokojo.service.NoopEmailSender;
import io.kodokojo.service.SmtpEmailSender;
import org.apache.commons.lang.StringUtils;

public class EmailSenderModule extends AbstractModule {
    @Override
    protected void configure() {
        //
    }

    @Singleton
    @Provides
    EmailSender provideEmailSender(EmailConfig emailConfig) {
        if (StringUtils.isBlank(emailConfig.smtpHost())) {
            return new NoopEmailSender();
        } else {
            return new SmtpEmailSender(emailConfig.smtpHost(), emailConfig.smtpPort(), emailConfig.smtpUsername(), emailConfig.smtpPassword(), emailConfig.smtpFrom());
        }
    }
}
