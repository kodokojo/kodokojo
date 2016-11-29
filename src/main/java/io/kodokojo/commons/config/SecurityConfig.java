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
package io.kodokojo.commons.config;

import io.kodokojo.commons.config.properties.Key;
import io.kodokojo.commons.config.properties.PropertyConfig;

public interface SecurityConfig extends PropertyConfig {

    @Key(value = "security.secretkey.path", defaultValue = "secret.key")
    String privateKeyPath();

    @Key("security.ssl.wildcardPemPath")
    String wildcardPemPath();

    String sslRootCaPemPath();

    @Key("security.ssl.rootCa.ks.alias")
    String sslRootCaKsAlias();

    @Key("security.ssl.rootCa.ks.password")
    String sslRootCaKsPassword();

}
