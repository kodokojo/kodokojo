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
import io.kodokojo.bdd.User;
import io.kodokojo.bdd.stage.ApplicationGiven;
import io.kodokojo.bdd.stage.ApplicationThen;
import io.kodokojo.bdd.stage.ApplicationWhen;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import org.junit.Rule;
import org.junit.Test;


@As("User modification scenarii")
@User
public class UserModificationIntTest extends ScenarioTest<ApplicationGiven<?>, ApplicationWhen<?>, ApplicationThen<?>> {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    @Test
    @DockerIsRequire
    public void create_a_simple_user_and_modify_password() {
        given().redis_is_started(dockerPresentMethodRule.getDockerTestSupport())
                .and().kodokojo_restEntrypoint_is_available();
        when().retrive_a_new_id()
                .and().create_user_with_email_$("jpthiery@xebia.fr")
                .and().update_user_$_with_password_$("jpthiery", "mypassword", false);
        then().it_exist_a_valid_user_with_username_$("jpthiery")
                .and().it_is_possible_to_get_complete_details_for_user_$("jpthiery")
                .and().user_$_belong_to_entity_$("jpthiery", "jpthiery@xebia.fr");
    }

}
