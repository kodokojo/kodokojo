package io.kodokojo.bdd.stage;

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

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AccessRestThen<SELF extends AccessRestThen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    int responseHttpStatusCode;

    @ExpectedScenarioState
    String responseHttpStatusBody;

    @ExpectedScenarioState
    boolean receiveWebSocketWelcome;


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
}
