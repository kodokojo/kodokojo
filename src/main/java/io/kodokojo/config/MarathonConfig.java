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
package io.kodokojo.config;

import io.kodokojo.config.properties.Key;
import io.kodokojo.config.properties.PropertyConfig;

public interface MarathonConfig extends PropertyConfig {

    @Key("marathon.url")
    String url();

    @Key(value = "marathon.ignore.constraint", defaultValue = "FALSE")
    Boolean ignoreContraint();

    @Key("marathon.login")
    String login();

    @Key("marathon.password")
    String password();

}
