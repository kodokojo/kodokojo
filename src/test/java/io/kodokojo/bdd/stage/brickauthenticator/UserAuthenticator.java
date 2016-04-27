package io.kodokojo.bdd.stage.brickauthenticator;

import com.squareup.okhttp.OkHttpClient;
import io.kodokojo.bdd.stage.UserInfo;

public interface UserAuthenticator {

    boolean authenticate(String url, UserInfo userInfo);

    boolean authenticate(OkHttpClient okHttpClient, String brickDomainUrl, UserInfo userInfo);

}
