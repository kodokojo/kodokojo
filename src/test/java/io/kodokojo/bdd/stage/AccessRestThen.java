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



import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import io.kodokojo.commons.endpoint.dto.BrickConfigDto;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AccessRestThen<SELF extends AccessRestThen<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessRestThen.class);

    @ExpectedScenarioState
    int responseHttpStatusCode;

    @ExpectedScenarioState
    String responseHttpStatusBody;

    @ExpectedScenarioState
    boolean receiveWebSocketWelcome;

    @ExpectedScenarioState
    List<BrickConfigDto> brickAvailable = new ArrayList<>();


    public SELF it_should_return_status_$(int expectedHttpStatus) {
        assertThat(responseHttpStatusCode).isEqualTo(expectedHttpStatus);
        return self();
    }

    public SELF it_receive_a_welcome_message() {
        assertThat(receiveWebSocketWelcome).isTrue();
        return self();
    }

    public SELF it_NOT_receive_a_welcome_message() {
        assertThat(receiveWebSocketWelcome).isFalse();
        return self();
    }

    public SELF it_receive_a_valide_list_of_available_brick() {
        assertThat(brickAvailable).isNotEmpty();
        LOGGER.debug("Available brick are : {}", StringUtils.join(brickAvailable, ","));
        return self();
    }
}
