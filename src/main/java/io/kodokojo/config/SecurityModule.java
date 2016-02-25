package io.kodokojo.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Named;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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
            try (FileOutputStream out = new FileOutputStream(securityConfig.privateKeyPath())) {
                out.write(res.getEncoded());
                out.flush();
                return res;
            } catch (IOException e) {
                throw new RuntimeException("unable to read and/or create key file at path " + keyFile.getAbsolutePath(), e);
            }
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
