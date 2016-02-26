package io.kodokojo.bdd.feature;

/*
 * #%L
 * project-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.bdd.API;
import io.kodokojo.bdd.User;
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
        given().kodokojo_is_running();
        when().try_to_access_to_get_url_$("/api/v1/doc");
        then().it_should_return_status_$(200);
    }

    @Test
    @DockerIsRequire
    public void anonymous_can_not_access_to_random_url() {
        given().kodokojo_is_running();
        when().try_to_access_to_get_url_$("/my/awersome/url/of/the/death");
        then().it_should_return_status_$(401);
    }

}
