package io.kodokojo.service;

import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.model.BootstrapStackData;

/**
 * Allow to store data and configuration which will be used to start project's brick.
 */
public interface ConfigurationStore {

    boolean storeBootstrapStackData(BootstrapStackData bootstrapStackData);

    boolean storeSSLKeys(String projectName, String brickTypeName, SSLKeyPair sslKeyPair);

}
