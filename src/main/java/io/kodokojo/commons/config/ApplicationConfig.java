/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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

public interface ApplicationConfig extends PropertyConfig {

    @Key(value = "application.port", defaultValue = "80")
    int port();

    @Key("application.dns.domain")
    String domain();

    @Key("lb.host")
    String loadbalancerHost();

    @Key(value = "initSshport", defaultValue = "32768")
    int initialSshPort();

    @Key(value = "ssl.ca.duration", defaultValue = "8035200000") //3 mouths
    long sslCaDuration();

    @Key(value = "user.creation.waitinglist", defaultValue = "false")
    Boolean userCreationRoutedInWaitingList();

}
