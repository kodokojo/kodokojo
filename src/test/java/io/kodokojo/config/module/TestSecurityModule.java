package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.service.RSAUtils;
import io.kodokojo.service.ssl.SSLKeyPair;
import io.kodokojo.service.ssl.SSLUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.inject.Named;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class TestSecurityModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    @Named("securityKey")
    SecretKey provideSecretKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            SecretKey aesKey = generator.generateKey();
            return aesKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to generate AES key", e);
        }
    }

    @Provides
    @Singleton
    SSLKeyPair provideSSLKeyPair() {
        try {
            KeyPair keyPair = RSAUtils.generateRsaKeyPair();
            return SSLUtils.createSelfSignedSSLKeyPair("kodokojo.dev", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to generate a RSA key for SSL root CA", e);
        }
    }
}
