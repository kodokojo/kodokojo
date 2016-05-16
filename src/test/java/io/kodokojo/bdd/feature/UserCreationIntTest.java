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


@As("User creation scenarios")
@User
public class UserCreationIntTest extends ScenarioTest<ApplicationGiven<?>, ApplicationWhen<?>, ApplicationThen<?>> {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    @Test
    @DockerIsRequire
    public void create_a_simple_user() {
        given().redis_is_started()
                .and().kodokojo_restEntrypoint_is_available();
        when().retrive_a_new_id()
                .and().create_user_with_email_$("jpthiery@xebia.fr");
        then().it_exist_a_valid_user_with_username_$("jpthiery")
                .and().it_is_possible_to_get_complete_details_for_user_$("jpthiery")
                .and().user_$_belong_to_entity_$("jpthiery", "jpthiery@xebia.fr");
    }



    @Test
    @DockerIsRequire
    public void create_two_users_with_same_identifier() {
        given().redis_is_started()
                .and().kodokojo_restEntrypoint_is_available();
        when().retrive_a_new_id()
                .and().create_user_with_email_$("jpthiery@xebia.fr")
                .and().create_user_with_email_$_which_must_fail("aletaxin@xebia.fr");

        then().it_exist_a_valid_user_with_username_$("jpthiery")
                .and().it_NOT_exist_a_valid_user_with_username_$("aletaxin");
    }

    @Test
    @DockerIsRequire
    public void create_two_users_and_get_details() {
        given().redis_is_started()
                .and().kodokojo_restEntrypoint_is_available()
                .and().i_will_be_user_$("jpthiery");
        when().retrive_a_new_id()
                .and().create_user_with_email_$("jpthiery@xebia.fr")
        .and().retrive_a_new_id()
                .and().create_user_with_email_$("aletaxin@xebia.fr");
        then().it_exist_a_valid_user_with_username_$("jpthiery")
                .and().it_exist_a_valid_user_with_username_$("aletaxin")
                .and().it_is_possible_to_get_complete_details_for_user_$("jpthiery")
        .and().it_is_possible_to_get_details_for_user_$("aletaxin")
        .and().it_is_NOT_possible_to_get_complete_details_for_user_$("aletaxin");
    }

    @Test
    @DockerIsRequire
    public void create_two_users_with_same_username() {
        given().redis_is_started()
                .and().kodokojo_restEntrypoint_is_available();
        when().retrive_a_new_id()
                .and().create_user_with_email_$("jpthiery@xebia.fr")
                .and().retrive_a_new_id()
                .and().create_user_with_email_$_which_must_fail("jpthiery@kodokojo.io");

        then().it_exist_a_valid_user_with_username_$("jpthiery");
    }

    @Test
    @DockerIsRequire
    public void try_to_create_user_without_identifier() {
        given().redis_is_started()
                .and().kodokojo_restEntrypoint_is_available();
        when().create_user_with_email_$_which_must_fail("aletaxin@xebia.fr");

        then().it_NOT_exist_a_valid_user_with_username_$("aletaxin");
    }

}
