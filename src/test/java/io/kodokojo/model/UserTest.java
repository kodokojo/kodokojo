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
package io.kodokojo.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {


    @Test
    public void construct_name_from_a_single_attribute() {
        User user = new User("1234", "1234", "Jean-Pascal THIERY", "jpthiery", "jpthiery@kodokojo.io", "jpthiery", "an ssh public key");
        assertThat(user.getName()).isEqualTo("Jean-Pascal THIERY");
        assertThat(user.getFirstName()).isEqualTo("Jean-Pascal");
        assertThat(user.getLastName()).isEqualTo("THIERY");

    }

}