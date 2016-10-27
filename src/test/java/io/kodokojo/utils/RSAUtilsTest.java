package io.kodokojo.utils;

import javaslang.control.Try;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class RSAUtilsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSAUtilsTest.class);

    @Test
    public void test_encrypt_decrypt() {

        SecretKey key = Try.of(() -> {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128);
            return kg.generateKey();
        }).getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);

        LOGGER.info("Using a key with algo = {}.", key.getAlgorithm());

        byte[] encryptObjectWithAES = RSAUtils.encryptObjectWithAES(key, new Bundle("Coucou"));
        Bundle bundleDecrypted = (Bundle) RSAUtils.decryptObjectWithAES(key, encryptObjectWithAES);
        assertThat(bundleDecrypted).isNotNull();
        assertThat(bundleDecrypted.getMessage()).isEqualTo("Coucou");
    }

    static class Bundle implements Serializable {

        private String message;

        Bundle(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}