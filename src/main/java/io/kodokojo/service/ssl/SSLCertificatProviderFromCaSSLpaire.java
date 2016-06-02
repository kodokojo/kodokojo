package io.kodokojo.service.ssl;

import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.service.SSLCertificatProvider;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public class SSLCertificatProviderFromCaSSLpaire implements SSLCertificatProvider {

    private final String domain;

    private final long caSslDuration;

    private final SSLKeyPair caKeypaire;

    private final Map<String, SSLKeyPair> cache;

    private final BrickUrlFactory brickUrlFactory;

    private final Object monitor = new Object();

    @Inject
    public SSLCertificatProviderFromCaSSLpaire(String domain, long caSslDuration, SSLKeyPair caKeypaire, BrickUrlFactory brickUrlFactory) {
        if (isBlank(domain)) {
            throw new IllegalArgumentException("domain must be defined.");
        }
        if (caKeypaire == null) {
            throw new IllegalArgumentException("caKeypaire must be defined.");
        }
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        this.domain = domain;
        this.caSslDuration = caSslDuration;
        this.caKeypaire = caKeypaire;
        this.cache = new HashMap<>();
        this.brickUrlFactory = brickUrlFactory;
    }

    @Override
    public SSLKeyPair provideCertificat(String projectName, String stackName, BrickConfiguration brickConfiguration) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }

        SSLKeyPair sslKeyPair = null;
        synchronized (monitor) {
            sslKeyPair = cache.get(projectName);
            if (sslKeyPair == null) {
                sslKeyPair = SSLUtils.createSSLKeyPair(String.format("%s.%s", projectName, domain),caKeypaire.getPrivateKey(), caKeypaire.getPublicKey(), caKeypaire.getCertificates(), caSslDuration, true);
                cache.put(projectName, sslKeyPair);
            }
        }
        return SSLUtils.createSSLKeyPair(brickUrlFactory.forgeUrl(projectName, stackName, brickConfiguration.getBrick().getName(), brickConfiguration.getName()), sslKeyPair.getPrivateKey(), sslKeyPair.getPublicKey(), sslKeyPair.getCertificates());
    }
}
