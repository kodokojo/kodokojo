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
package io.kodokojo.commons.config.module;

import io.kodokojo.commons.config.SecurityConfig;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class SecurityModuleTest {

    @Test
    public void loading_sslKeyPaire_from_keystore() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidKeySpecException, IOException {
        SecurityConfig securityConfig = new SecurityConfig(){

            @Override
            public String privateKeyPath() {
                return null;
            }

            @Override
            public String wildcardPemPath() {
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