package io.kodokojo.commons.utils.ssl;

/*
 * #%L
 * kodokojo-commons
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class SSLKeyPair implements Serializable {

    private final RSAPrivateKey privateKey;

    private final RSAPublicKey publicKey;

    private final X509Certificate[] certificates;

    public SSLKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey, X509Certificate[] certificates) {
        if (privateKey == null) {
            throw new IllegalArgumentException("privateKey must be defined.");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKey must be defined.");
        }
        if (certificates == null) {
            throw new IllegalArgumentException("certificates must be defined.");
        }
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.certificates = certificates;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public X509Certificate[] getCertificates() {
        return certificates;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return "SSLKeyPair{" +
                "privateKey=" + privateKey +
                ", publicKey=" + publicKey +
                ", certificates=" + StringUtils.join(certificates, ",") +
                '}';
    }
}
