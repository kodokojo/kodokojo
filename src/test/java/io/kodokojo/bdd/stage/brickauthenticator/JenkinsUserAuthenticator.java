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

public class JenkinsUserAuthenticator implements UserAuthenticator {
    @Override
    public boolean authenticate(String url, UserInfo userInfo) {
        OkHttpClient httpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url + "/me/configure");
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
