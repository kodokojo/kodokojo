package io.kodokojo.bdd.stage.brickauthenticator;

import com.squareup.okhttp.OkHttpClient;
import io.kodokojo.bdd.stage.UserInfo;

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
