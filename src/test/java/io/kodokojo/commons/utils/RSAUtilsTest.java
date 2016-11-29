/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.commons.utils;

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