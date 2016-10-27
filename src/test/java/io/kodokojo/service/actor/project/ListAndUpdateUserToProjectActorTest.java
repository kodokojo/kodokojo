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
package io.kodokojo.service.actor.project;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import akka.util.Timeout;
import io.kodokojo.model.*;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.test.utils.DataBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListAndUpdateUserToProjectActorTest implements DataBuilder {


    private static ActorSystem actorSystem;

    private static TestActorRef endpointRef;

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
        endpointRef = TestActorRef.create(actorSystem,Props.create(TestEndpointActor.class), "endpoint");
    }

    @Test
    public void list_and_update_user_for_two_bricks_from_on_project() {
        // given
        User requester = anUser();

        ProjectRepository projectRepository = mock(ProjectRepository.class);
        TestActorRef<Actor> subject = TestActorRef.create(actorSystem, ListAndUpdateUserToProjectActor.PROPS(projectRepository));

        // when

        Set<String> projectIds = Collections.singleton("9000");
        when(projectRepository.getProjectConfigIdsByUserIdentifier("1234")).thenReturn(projectIds);

        when(projectRepository.getProjectConfigurationById("9000")).thenReturn(aProjectConfiguration());

        //then
        Future<Object> future = ask(subject, new ProjectUpdaterMessages.ListAndUpdateUserToProjectMsg(requester, new UpdateData<>(requester, requester)), Timeout.apply(1, TimeUnit.SECONDS));
        try {
            Object result = Await.result(future, Duration.apply(1, TimeUnit.SECONDS));
            assertThat(result).isNotNull();
            assertThat(result.getClass()).isEqualTo(ProjectUpdaterMessages.ListAndUpdateUserToProjectResultMsg.class);
            TestEndpointActor underlyingActor = (TestEndpointActor) endpointRef.underlyingActor();
            Set<Object> messages = underlyingActor.getMessages();
            assertThat(messages.size()).isEqualTo(3);
            messages.stream().forEach(m -> {
                assertThat(m.getClass()).isEqualTo(BrickUpdateUserActor.BrickUpdateUserMsg.class);
            });

        } catch (Exception e) {
            fail(e.getMessage());
        }


    }

    @AfterClass
    public static void tearDown() {
        JavaTestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

    private static class TestEndpointActor extends AbstractActor {

        private final Set<Object> messages = new HashSet<>();

        public TestEndpointActor() {
            receive(ReceiveBuilder.match(BrickUpdateUserActor.BrickUpdateUserMsg.class, msg -> {
                messages.add(msg);
                sender().tell(new BrickUpdateUserActor.BrickUpdateUserResultMsg(msg, true), self());
            }).matchAny(messages::add).build());
        }

        public Set<Object> getMessages() {
            return new HashSet<>(messages);
        }
    }

}