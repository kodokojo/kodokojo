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
package io.kodokojo.brick;


import okhttp3.Request;

import java.util.Base64;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public interface BrickConfigurerHelper {

    default void addBasicAuthentificationHeader(Request.Builder builder, String login, String password) {
        requireNonNull(builder, "builder must be defined.");
        if (isBlank(login)) {
            throw new IllegalArgumentException("login must be defined.");
        }
        if (isBlank(password)) {
            throw new IllegalArgumentException("password must be defined.");
        }
        String encodedCredentials = Base64.getEncoder().encodeToString(String.format("%s:%s", login, password).getBytes());
        builder.addHeader("Authorization", "Basic " + encodedCredentials);
    }

}
