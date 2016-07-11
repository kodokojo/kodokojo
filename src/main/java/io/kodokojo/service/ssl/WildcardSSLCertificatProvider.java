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
package io.kodokojo.service.ssl;

import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.service.SSLCertificatProvider;

public class WildcardSSLCertificatProvider implements SSLCertificatProvider {

    private final SSLKeyPair sslKeyPair;

    public WildcardSSLCertificatProvider(SSLKeyPair sslKeyPair) {
        if (sslKeyPair == null) {
            throw new IllegalArgumentException("sslKeyPair must be defined.");
        }
        this.sslKeyPair = sslKeyPair;
    }

    @Override
    public SSLKeyPair provideCertificat(String projectName, String stackName, BrickConfiguration brickConfiguration) {
        return sslKeyPair;
    }

}
