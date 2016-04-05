package io.kodokojo.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {


    @Test
    public void construct_name_from_a_single_attribute() {
        User user = new User("1234", "Jean-Pascal THIERY", "jpthiery", "jpthiery@kodokojo.io", "jpthiery", "an ssh public key");
        assertThat(user.getName()).isEqualTo("Jean-Pascal THIERY");
        assertThat(user.getFirstName()).isEqualTo("Jean-Pascal");
        assertThat(user.getLastName()).isEqualTo("THIERY");

    }

}