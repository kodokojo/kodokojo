package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.brick.*;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.model.Brick;
import io.kodokojo.model.User;
import io.kodokojo.brick.gitlab.GitlabConfigurer;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.fail;

public class BrickConfigurerWhen<SELF extends BrickConfigurerWhen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    String brickName;

    @ExpectedScenarioState
    String brickUrl;

    @ExpectedScenarioState
    BrickFactory brickFactory;

    @ExpectedScenarioState
    BrickConfigurerProvider brickConfigurerProvider;

    @ProvidedScenarioState
    BrickConfigurer brickConfigurer;

    @ProvidedScenarioState
    UserInfo defaultUserInfo;

    @ProvidedScenarioState
    BrickConfigurerData brickConfigurerData;

    public SELF i_create_a_default_user() {
        defaultUserInfo = new UserInfo("jpthiery", "123456", "jpthiery", "jpthiery@kodokojo.io");
        Brick brick = brickFactory.createBrick(brickName);
        brickConfigurer = brickConfigurerProvider.provideFromBrick(brick);
        KeyPair keyPair = null;
        try {
            keyPair = RSAUtils.generateRsaKeyPair();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String sshPublicKey = RSAUtils.encodePublicKey(publicKey, defaultUserInfo.getEmail());
        User defaultUser = new User(defaultUserInfo.getIdentifier(), "Jean-Pascal THIERY", defaultUserInfo.getUsername(), defaultUserInfo.getEmail(), defaultUserInfo.getPassword(), sshPublicKey);
        List<User> users = Collections.singletonList(defaultUser);

        BrickConfigurerData configurationData = new BrickConfigurerData("Acme", "build-A", brickUrl, "kodokojo.dev", users, users);
        configurationData.getContext().put(GitlabConfigurer.GITLAB_FORCE_ENTRYPOINT_KEY, Boolean.TRUE); //Specific config for Gitlab.
        try {
            configurationData = brickConfigurer.configure(configurationData);
            this.brickConfigurerData = brickConfigurer.addUsers(configurationData, users);
        } catch (BrickConfigurationException e) {
            fail(e.getMessage());
        }
        return self();
    }

}
