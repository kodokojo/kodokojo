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
package io.kodokojo.bdd.feature;



import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.bdd.API;
import io.kodokojo.bdd.stage.*;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import org.junit.Rule;
import org.junit.Test;


@As("REST access right")
@API
public class AccessRestIntTest extends ScenarioTest<ApplicationGiven<?>, AccessRestWhen<?>, AccessRestThen<?>> {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    @Test
    @DockerIsRequire
    public void anonymous_user_access_to_api_documentation() {
        given().kodokojo_is_running(dockerPresentMethodRule.getDockerTestSupport());
        when().try_to_access_to_get_url_$("/api/v1/doc");
        then().it_should_return_status_$(200);
    }

    @Test
    @DockerIsRequire
    public void anonymous_user_access_to_api_version() {
        given().kodokojo_is_running(dockerPresentMethodRule.getDockerTestSupport());
        when().try_to_access_to_get_url_$("/api/v1");
        then().it_should_return_status_$(200);
    }

    @Test
    @DockerIsRequire
    public void anonymous_can_not_access_to_random_url() {
        given().kodokojo_is_running(dockerPresentMethodRule.getDockerTestSupport());
        when().try_to_access_to_get_url_$("/my/awersome/url/of/the/death");
        then().it_should_return_status_$(401);
    }

    @Test
    @DockerIsRequire
    public void user_connect_to_websocket_event() {
        given().kodokojo_is_running(dockerPresentMethodRule.getDockerTestSupport())
                .and().i_am_user_$("jpthiery", true);
        when().try_to_access_to_events_websocket();
        then().it_receive_a_welcome_message();
    }
    @Test
    @DockerIsRequire
    public void anonymous_user_fail_connect_to_websocket_event() {
        given().kodokojo_is_running(dockerPresentMethodRule.getDockerTestSupport());
        when().try_to_access_to_events_websocket_as_anonymous();
        then().it_NOT_receive_a_welcome_message();
    }

}
