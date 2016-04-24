package io.kodokojo.bdd.stage.brickauthenticator;

import io.kodokojo.bdd.stage.UserInfo;

public interface UserAuthenticator {

    boolean authenticate(String url, UserInfo userInfo);

}
