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
package io.kodokojo.bdd.stage.brickauthenticator;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.kodokojo.bdd.stage.StageUtils;
import io.kodokojo.bdd.stage.UserInfo;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class NexusUserAuthenticator implements UserAuthenticator {
    @Override
    public boolean authenticate(String url, UserInfo userInfo) {
        OkHttpClient httpClient = new OkHttpClient();
        return authenticate(httpClient, url, userInfo);
    }

    @Override
    public boolean authenticate(OkHttpClient httpClient, String url, UserInfo userInfo) {

        Request.Builder builder = new Request.Builder().url(url + "/service/local/users");
        builder = StageUtils.addBasicAuthentification(userInfo, builder);
        Request request =  builder.build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            assertThat(response.code()).isBetween(200, 299);
            return true;
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return false;
    }
}
