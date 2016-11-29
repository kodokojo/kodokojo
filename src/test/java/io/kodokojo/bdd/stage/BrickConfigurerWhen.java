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
package io.kodokojo.bdd.stage;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.brick.*;
import io.kodokojo.brick.gitlab.GitlabConfigurer;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.model.*;
import io.kodokojo.commons.utils.RSAUtils;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.fail;

public class BrickConfigurerWhen<SELF extends BrickConfigurerWhen<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickConfigurerWhen.class);

    @ExpectedScenarioState
    DockerTestSupport dockerTestSupport = new DockerTestSupport();

    @ExpectedScenarioState
    String containerId;

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
        defaultUserInfo = new UserInfo("jpthiery", "123456", "67899", "jpthiery1234", "jpthiery@kodokojo.io");
        BrickConfiguration brickConfiguration = brickFactory.createBrick(brickName);
        brickConfigurer = brickConfigurerProvider.provideFromBrick(brickConfiguration);
        KeyPair keyPair = null;
        try {
            keyPair = RSAUtils.generateRsaKeyPair();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String sshPublicKey = RSAUtils.encodePublicKey(publicKey, defaultUserInfo.getEmail());
        User defaultUser = new User(defaultUserInfo.getIdentifier(), "1234", "Jean-Pascal THIERY", defaultUserInfo.getUsername(), defaultUserInfo.getEmail(), defaultUserInfo.getPassword(), sshPublicKey);
        List<User> users = Collections.singletonList(defaultUser);

        UserService userService = new UserService("5678", "service-acme", "serviceacme", "toto1234", (RSAPrivateKey) keyPair.getPrivate(), publicKey);
        ArrayList<User> admins = new ArrayList<>();
        admins.add(defaultUser);
        HashSet<StackConfiguration> stackConfigurations = new HashSet<>();
        stackConfigurations.add(Mockito.mock(StackConfiguration.class));
        ProjectConfiguration fakeProjectConfiguration = new ProjectConfiguration("1234", "acme", userService, admins, stackConfigurations, new ArrayList<>());

        BrickConfigurerData configurationData = new BrickConfigurerData("Acme", "build-A", brickUrl, "kodokojo.dev", users, users);
        configurationData.addInContext(GitlabConfigurer.GITLAB_FORCE_ENTRYPOINT_KEY, Boolean.TRUE); //Specific config for Gitlab.
        try {
            configurationData = brickConfigurer.configure(fakeProjectConfiguration, configurationData);
            this.brickConfigurerData = brickConfigurer.addUsers(fakeProjectConfiguration, configurationData, users);
        } catch (BrickConfigurationException e) {
            dockerTestSupport.getDockerClient().logContainerCmd(containerId).withStdErr(true).withStdOut(true).withTailAll().exec(new ResultCallback<Frame>() {
                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onNext(Frame object) {
                    String msg = new String(object.getPayload());
                    if (msg.endsWith("\n")) {
                        msg = msg.substring(0, msg.length() - "\n".length());
                    }
                    LOGGER.error(msg);
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void close() throws IOException {

                }
            });
            fail(e.getMessage());
        }
        return self();
    }

}
