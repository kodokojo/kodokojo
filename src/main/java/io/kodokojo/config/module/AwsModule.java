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
package io.kodokojo.config.module;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.AwsConfig;
import io.kodokojo.config.EmailConfig;
import io.kodokojo.service.EmailSender;
import io.kodokojo.service.NoopEmailSender;
import io.kodokojo.service.SmtpEmailSender;
import io.kodokojo.service.aws.Route53DnsManager;
import io.kodokojo.service.aws.SesEmailSender;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.dns.NoOpDnsManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsModule.class);

    @Override
    protected void configure() {
        // Nothing to do.
    }

    @Provides
    @Singleton
    DnsManager provideDnsManager(ApplicationConfig applicationConfig, AwsConfig awsConfig) {
        AWSCredentials credentials = null;
        try {
            DefaultAWSCredentialsProviderChain defaultAWSCredentialsProviderChain = new DefaultAWSCredentialsProviderChain();
            credentials = defaultAWSCredentialsProviderChain.getCredentials();
        } catch (RuntimeException e) {
            LOGGER.warn("Unable to retrieve AWS credentials.");
        }
        if (StringUtils.isNotBlank(System.getenv("NO_DNS")) || credentials == null) {
            LOGGER.info("Using NoOpDnsManager as DnsManger implementation");
            return new NoOpDnsManager();
        } else {
            LOGGER.info("Using Route53DnsManager as DnsManger implementation");
            return new Route53DnsManager(applicationConfig.domain(), Region.getRegion(Regions.fromName(awsConfig.region())));
        }
    }

    @Provides
    @Singleton
    EmailSender provideEmailSender(AwsConfig awsConfig, EmailConfig emailConfig) {
        if (StringUtils.isBlank(emailConfig.smtpHost())) {
            AWSCredentials credentials = null;
            try {
                DefaultAWSCredentialsProviderChain defaultAWSCredentialsProviderChain = new DefaultAWSCredentialsProviderChain();
                credentials = defaultAWSCredentialsProviderChain.getCredentials();
            } catch (RuntimeException e) {
                LOGGER.warn("Unable to retrieve AWS credentials.");
            }
            if (credentials == null) {
                return new NoopEmailSender();
            } else {
                return new SesEmailSender(emailConfig.smtpFrom(), Region.getRegion(Regions.fromName(awsConfig.region())));
            }
        } else {
            return new SmtpEmailSender(emailConfig.smtpHost(), emailConfig.smtpPort(), emailConfig.smtpUsername(), emailConfig.smtpPassword(), emailConfig.smtpFrom());
        }
    }
}
