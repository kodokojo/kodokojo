package io.kodokojo.bdd.stage.brickauthenticator;

import com.squareup.okhttp.OkHttpClient;
import io.kodokojo.bdd.stage.UserInfo;
import io.kodokojo.project.gitlab.GitlabConfigurer;

public class GitlabUserAuthenticator implements UserAuthenticator {

    @Override
    public boolean authenticate(String url, UserInfo userInfo) {
        OkHttpClient httpClient = GitlabConfigurer.provideDefaultOkHttpClient();
        return GitlabConfigurer.signIn(httpClient, url, userInfo.getUsername(), userInfo.getPassword());
    }

}
