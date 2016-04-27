package io.kodokojo.bdd.stage.brickauthenticator;

import com.squareup.okhttp.OkHttpClient;
import io.kodokojo.bdd.stage.UserInfo;
import io.kodokojo.project.gitlab.GitlabConfigurer;

public class GitlabUserAuthenticator implements UserAuthenticator {

    @Override
    public boolean authenticate(String url, UserInfo userInfo) {
        OkHttpClient httpClient = GitlabConfigurer.provideDefaultOkHttpClient();
        return authenticate(httpClient, url, userInfo);
    }

    @Override
    public boolean authenticate(OkHttpClient httpClient, String brickDomainUrl, UserInfo userInfo) {
        return GitlabConfigurer.signIn(httpClient, brickDomainUrl, userInfo.getUsername(), userInfo.getPassword());
    }

}
