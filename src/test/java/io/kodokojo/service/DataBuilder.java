package io.kodokojo.service;

import io.kodokojo.config.MarathonConfig;
import io.kodokojo.model.Project;
import io.kodokojo.model.Stack;
import io.kodokojo.model.StackType;
import io.kodokojo.model.User;
import scala.concurrent.duration.FiniteDuration;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public interface DataBuilder {

    int thirtySeconds = 30000;

    FiniteDuration twoSeconds = FiniteDuration.create(2, TimeUnit.SECONDS);

    default Project aProjectWithStacks(HashSet<Stack> stacks) {
        return new Project("1234", "test", new Date(), stacks);
    }

    default HashSet<Stack> aBuildStack() {
        HashSet<Stack> stacks = new HashSet<>();
        stacks.add(new Stack("build-A", StackType.BUILD, new HashSet<>()));
        return stacks;
    }

    default User anUser() {
        return new User("1234", "5678", "John Doe", "jdoe", "jdoe@inconnu.com", "jdoe4ever", "ssh key");
    }

    default MarathonConfig aMarathonConfig() {
        return new MarathonConfig() {
            @Override
            public String url() {
                return "http://localhost:8080";
            }

            @Override
            public Boolean ignoreContraint() {
                return Boolean.TRUE;
            }

            @Override
            public String login() {
                return "";
            }

            @Override
            public String password() {
                return "";
            }
        };
    }

}
