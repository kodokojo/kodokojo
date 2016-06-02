package io.kodokojo.service.ssl;

import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.service.SSLCertificatProvider;

public class WildcardSSLCertificatProvider implements SSLCertificatProvider {

    private final SSLKeyPair sslKeyPair;

    public WildcardSSLCertificatProvider(SSLKeyPair sslKeyPair) {
        if (sslKeyPair == null) {
            throw new IllegalArgumentException("sslKeyPair must be defined.");
        }
        this.sslKeyPair = sslKeyPair;
    }

    @Override
    public SSLKeyPair provideCertificat(String projectName, String stackName, BrickConfiguration brickConfiguration) {
        return sslKeyPair;
    }

}
