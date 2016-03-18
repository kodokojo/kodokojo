package io.kodokojo.service;

import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.model.BootstrapStackData;

public interface ConfigurationStore {

    boolean storeBootstrapStackData(BootstrapStackData bootstrapStackData);

    boolean storeSSLKeys(String projectName, String brickTypeName, SSLKeyPair sslKeyPair);

}
