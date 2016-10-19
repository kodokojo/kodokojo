/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.bdd.stage.brickauthenticator;

import io.kodokojo.bdd.stage.UserInfo;
import okhttp3.OkHttpClient;

public class DockerRegistryUserAuthenticator implements UserAuthenticator {

    @Override
    public boolean authenticate(String url, UserInfo userInfo) {
        return true;
    }

    @Override
    public boolean authenticate(OkHttpClient okHttpClient, String brickDomainUrl, UserInfo userInfo) {
        return true;
    }
}
