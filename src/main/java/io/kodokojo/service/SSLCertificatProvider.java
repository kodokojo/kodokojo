package io.kodokojo.service;

import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.model.BrickConfiguration;

public interface SSLCertificatProvider {

    SSLKeyPair provideCertificat(String projectName, String stackName, BrickConfiguration brickConfiguration);
}
