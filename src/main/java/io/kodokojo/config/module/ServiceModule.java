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
import com.squareup.okhttp.OkHttpClient;
import io.kodokojo.brick.*;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.ssl.SSLKeyPair;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.SecurityConfig;
import io.kodokojo.endpoint.UserAuthenticator;
import io.kodokojo.service.*;
import io.kodokojo.service.authentification.SimpleCredential;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.service.ssl.SSLCertificatProviderFromCaSSLpaire;
import io.kodokojo.service.ssl.WildcardSSLCertificatProvider;
import org.apache.commons.lang.StringUtils;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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
    BrickStateMsgDispatcher provideBrickStateMsgDispatcher(ProjectRepository projectRepository) {
        BrickStateMsgDispatcher dispatcher = new BrickStateMsgDispatcher();
        StoreBrickStateListener storeBrickStateListener = new StoreBrickStateListener(projectRepository);
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
    BrickConfigurerProvider provideBrickConfigurerProvider(BrickUrlFactory brickUrlFactory, OkHttpClient httpClient) {
        return new DefaultBrickConfigurerProvider(brickUrlFactory, httpClient);
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
    ProjectManager provideProjectManager(ApplicationConfig applicationConfig, BrickConfigurationStarter brickConfigurationStarter, ConfigurationStore configurationStore, ProjectRepository projectRepository, BootstrapConfigurationProvider bootstrapConfigurationProvider, DnsManager dnsManager, BrickConfigurerProvider brickConfigurerProvider, BrickUrlFactory brickUrlFactory) {
        return new DefaultProjectManager(applicationConfig.domain(), configurationStore, projectRepository, bootstrapConfigurationProvider,dnsManager, brickConfigurerProvider,  brickConfigurationStarter, brickUrlFactory);
    }

    @Provides
    @Singleton
    BrickUrlFactory provideBrickUrlFactory(ApplicationConfig applicationConfig) {
        return new DefaultBrickUrlFactory(applicationConfig.domain());
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {

        // TODO : Replace this implementation by an httpClient which integrate the SSL CA used to create services.

        OkHttpClient httpClient = new OkHttpClient();
        final TrustManager[] certs = new TrustManager[]{new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain,
                                           final String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain,
                                           final String authType) throws CertificateException {
            }
        }};

        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, certs, new SecureRandom());
        } catch (final java.security.GeneralSecurityException ex) {
            //
        }
        httpClient.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        httpClient.setSslSocketFactory(ctx.getSocketFactory());
        return httpClient;
    }

}
