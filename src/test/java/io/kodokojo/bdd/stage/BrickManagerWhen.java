package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.model.Brick;
import io.kodokojo.model.User;
import io.kodokojo.project.gitlab.GitlabConfigurer;
import io.kodokojo.project.starter.BrickConfigurer;
import io.kodokojo.project.starter.ConfigurerData;
import io.kodokojo.service.BrickConfigurerProvider;
import io.kodokojo.service.BrickFactory;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.fail;

public class BrickManagerWhen<SELF extends BrickManagerWhen<?>> extends Stage<SELF> {

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
    ConfigurerData configurerData;

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

        ConfigurerData configurationData = new ConfigurerData("Acme", brickUrl, "kodokojo.dev", defaultUser, users);
        configurationData.getContext().put(GitlabConfigurer.GITLAB_FORCE_ENTRYPOINT_KEY, Boolean.TRUE); //Specific config for Gitlab.

        configurationData = brickConfigurer.configure(configurationData);
        this.configurerData = brickConfigurer.addUsers(configurationData, users);

        return self();
    }

}
