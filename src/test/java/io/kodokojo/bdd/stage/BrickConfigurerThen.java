package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.bdd.stage.brickauthenticator.UserAuthenticator;

import static org.assertj.core.api.Assertions.assertThat;

public class BrickConfigurerThen<SELF extends BrickConfigurerThen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    UserAuthenticator userAuthenticator;

    @ExpectedScenarioState
    String brickUrl;

    @ExpectedScenarioState
    UserInfo defaultUserInfo;

    public SELF it_is_possible_to_be_log_on_$_with_default_user(@Quoted String brickName) {
        boolean isAuthenticated = userAuthenticator.authenticate(brickUrl, defaultUserInfo);
        assertThat(isAuthenticated).isTrue();
        return self();
    }
}
