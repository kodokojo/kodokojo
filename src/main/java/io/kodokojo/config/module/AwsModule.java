package io.kodokojo.config.module;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.AwsConfig;
import io.kodokojo.service.aws.Route53DnsManager;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.dns.NoOpDnsManager;

public class AwsModule extends AbstractModule {
    @Override
    protected void configure() {
        // Nothing to do.
    }

    @Provides
    @Singleton
    DnsManager provideDnsManager(ApplicationConfig applicationConfig, AwsConfig awsConfig) {
        if (System.getenv("AWS_SECRET_ACCESS_KEY") == null) {
            return new NoOpDnsManager();
        } else {
            return new Route53DnsManager(applicationConfig.domain(), Region.getRegion(Regions.fromName(awsConfig.region())));
        }
    }
}
