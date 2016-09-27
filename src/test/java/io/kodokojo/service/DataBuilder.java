package io.kodokojo.service;

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

    default User aUser() {
        return new User("1234", "5678", "John Doe", "jdoe", "jdoe@inconnu.com", "jdoe4ever", "ssh key");
    }
}
