package io.kodokojo.config.module;

import io.kodokojo.config.SecurityConfig;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.*;

public class SecurityModuleTest {

    @Test
    public void loading_sslKeyPaire_from_keystore() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidKeySpecException, IOException {
        SecurityConfig securityConfig = new SecurityConfig(){

            @Override
            public String privateKeyPath() {
                return null;
            }

            @Override
            public String sslRootCaPemPath() {
                String keystorePAth = new File("").getAbsolutePath() + "/src/test/resources/keystore/mykeystore.jks";
                System.out.println("Keystore : " + keystorePAth);
                return keystorePAth;
            }

            @Override
            public String sslRootCaKsAlias() {
                return "rootcafake";
            }

            @Override
            public String sslRootCaKsPassword() {
                return "password";
            }
        };
    }

}