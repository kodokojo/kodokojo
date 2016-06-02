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
import com.google.inject.TypeLiteral;
import io.kodokojo.brick.*;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.SecurityConfig;
import io.kodokojo.endpoint.UserAuthenticator;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.service.*;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.ssl.SSLCertificatProviderFromCaSSLpaire;
import io.kodokojo.service.ssl.WildcardSSLCertificatProvider;
import io.kodokojo.service.store.ProjectStore;
import io.kodokojo.service.authentification.SimpleCredential;
import org.apache.commons.lang.StringUtils;

import java.io.File;

public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<UserAuthenticator<SimpleCredential>>() {/**/
        }).toProvider(SimpleUserAuthenticatorProvider.class);
    }

    @Provides
    @Singleton
    ApplicationLifeCycleManager provideApplicationLifeCycleManager() {
        return new ApplicationLifeCycleManager();
    }

    @Provides
    @Singleton
    BrickStateMsgDispatcher provideBrickStateMsgDispatcher(ProjectStore projectStore) {
        BrickStateMsgDispatcher dispatcher = new BrickStateMsgDispatcher();
        StoreBrickStateListener storeBrickStateListener = new StoreBrickStateListener(projectStore);
        dispatcher.addListener(storeBrickStateListener);
        return dispatcher;
    }

    @Provides
    @Singleton
    BrickFactory provideBrickFactory() {
        return new DefaultBrickFactory();
    }

    @Provides
    @Singleton
    BrickConfigurerProvider provideBrickConfigurerProvider(BrickUrlFactory brickUrlFactory) {
        return new DefaultBrickConfigurerProvider(brickUrlFactory);
    }

    @Provides
    @Singleton
    SSLCertificatProvider provideSslCertificatProvider(SecurityConfig securityConfig, ApplicationConfig applicationConfig, SSLKeyPair sslKeyPair, BrickUrlFactory brickUrlFactory) {
        if (StringUtils.isNotBlank(securityConfig.wildcardPemPath())) {
            return new WildcardSSLCertificatProvider(sslKeyPair);
        }
        return new SSLCertificatProviderFromCaSSLpaire(applicationConfig.domain(), applicationConfig.sslCaDuration(), sslKeyPair, brickUrlFactory);
    }

    @Provides
    @Singleton
    ProjectManager provideProjectManager(ApplicationConfig applicationConfig,  BrickConfigurationStarter brickConfigurationStarter, ConfigurationStore configurationStore, ProjectStore projectStore, BootstrapConfigurationProvider bootstrapConfigurationProvider, DnsManager dnsManager, BrickConfigurerProvider brickConfigurerProvider, BrickUrlFactory brickUrlFactory) {
        return new DefaultProjectManager(applicationConfig.domain(), configurationStore, projectStore, bootstrapConfigurationProvider,dnsManager, brickConfigurerProvider,  brickConfigurationStarter, brickUrlFactory);
    }

    @Provides
    @Singleton
    BrickUrlFactory provideBrickUrlFactory(ApplicationConfig applicationConfig) {
        return new DefaultBrickUrlFactory(applicationConfig.domain());
    }

}
