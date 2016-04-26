package io.kodokojo.bdd.feature;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.bdd.stage.BrickConfigurerGiven;
import io.kodokojo.bdd.stage.BrickConfigurerThen;
import io.kodokojo.bdd.stage.BrickConfigurerWhen;
import io.kodokojo.bdd.stage.brickauthenticator.GitlabUserAuthenticator;
import io.kodokojo.bdd.stage.brickauthenticator.JenkinsUserAuthenticator;
import io.kodokojo.bdd.stage.brickauthenticator.UserAuthenticator;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(DataProviderRunner.class)
public class BrickConfigurerIntTest extends ScenarioTest<BrickConfigurerGiven<?>, BrickConfigurerWhen<?>, BrickConfigurerThen<?>> {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    @DataProvider
    public static Object[][] brickData() {
        return new Object[][]{
                //  Name, Docker imagen name, containerPort, Timeout in seconde unit, User Logger
                {"Gitlab", "gitlab/gitlab-ce:8.5.8-ce.0", 80, 120, new GitlabUserAuthenticator()},
                {"Jenkins", "jenkins:1.651.1-alpine", 8080, 120, new JenkinsUserAuthenticator()}
                //{"Jenkins", "jenkins:2.0-alpine", 8080, 120, new JenkinsUserAuthenticator()}
        };
    }

    @Test
    @DockerIsRequire
    @UseDataProvider("brickData")
    public void run_and_configure_brick(String brickName, String imageName, int exposedPort, int timeout, UserAuthenticator userAuthenticator) {

        given().$_is_started(brickName, imageName, exposedPort, timeout, userAuthenticator);
        when().i_create_a_default_user();
        then().it_is_possible_to_be_log_on_$_with_default_user(brickName);
    }

}