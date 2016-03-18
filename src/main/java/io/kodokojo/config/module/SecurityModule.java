package io.kodokojo.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.config.SecurityConfig;
import org.apache.commons.io.FileUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SecurityModule extends AbstractModule {

    @Override
    protected void configure() {
        // Nothing to do.
    }

    @Provides
    @Singleton
    @Named("securityKey")
    SecretKey provideSecretKey(SecurityConfig securityConfig) {
        if (securityConfig == null) {
            throw new IllegalArgumentException("securityConfig must be defined.");
        }
        File keyFile = createPrivateKeyFile(securityConfig);
        if (keyFile.exists() && keyFile.canRead()) {
            return provideAesKey(keyFile);
        } else {
            SecretKey res = generateAesKey();
            try {
                keyFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create " + keyFile.getAbsolutePath() + " file.", e);
            }
            try (FileOutputStream out = new FileOutputStream(securityConfig.privateKeyPath())) {
                out.write(res.getEncoded());
                out.flush();
                return res;
            } catch (IOException e) {
                throw new RuntimeException("unable to read and/or create key file at path " + keyFile.getAbsolutePath(), e);
            }
        }
    }

    @Provides
    @Singleton
    SSLKeyPair provideSSLKeyPair(SecurityConfig securityConfig) {
        if (securityConfig == null) {
            throw new IllegalArgumentException("securityConfig must be defined.");
        }
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(System.getProperty("javax.net.ssl.keyStore")), System.getProperty("javax.net.ssl.keyStorePassword", "").toCharArray());

            RSAPrivateCrtKey key = (RSAPrivateCrtKey) ks.getKey(securityConfig.sslRootCaKsAlias(), securityConfig.sslRootCaKsPassword().toCharArray());
            if (key == null) {
                return null;
            }

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
            Certificate[] certificateChain = ks.getCertificateChain(securityConfig.sslRootCaKsAlias());
            List<X509Certificate> x509Certificates = Arrays.asList(certificateChain).stream().map(c -> (X509Certificate) c).collect(Collectors.toList());

            return new SSLKeyPair(key, publicKey, x509Certificates.toArray(new X509Certificate[x509Certificates.size()]));
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | InvalidKeySpecException | CertificateException | IOException e) {
            throw new RuntimeException("Unable to open default Keystore", e);
        }
    }

    private SecretKey generateAesKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            return kg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to get key generator for AES", e);
        }
    }

    private SecretKey provideAesKey(File keyFile) {
        try {
            byte[] keyByteArray = FileUtils.readFileToByteArray(keyFile);
            return new SecretKeySpec(keyByteArray, "AES");
        } catch (IOException e) {
            throw new RuntimeException("Unable to read key file from following path: '" + keyFile.getAbsolutePath() + "'.", e);
        }
    }

    //  For testing entry point.
    File createPrivateKeyFile(SecurityConfig securityConfig) {
        return new File(securityConfig.privateKeyPath());
    }

    //  For testing entry point.
    String readPrivateKeyFile(File privateRsaKeyFile) throws IOException {
        return FileUtils.readFileToString(privateRsaKeyFile);
    }

}
