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

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import io.kodokojo.model.*;
import io.kodokojo.service.DataBuilder;
import io.kodokojo.service.repository.ProjectRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProjectUpdaterActorTest implements DataBuilder {

    private static ActorSystem actorSystem;

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
    }

    @Test
    public void update_project_from_unknown_requester() {
        // Given
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        TestActorRef<Actor> subject = TestActorRef.create(actorSystem, ProjectUpdaterActor.props(projectRepository));

        // When
        Patterns.ask(subject, new ProjectUpdaterMessages.ProjectUpdateMsg(null, aProjectWithStacks(aBuildStack())), thirtySeconds);

        // Then
        verify(projectRepository).updateProject(any(Project.class));
    }

    @Test
    public void update_project_from_authorized_requester() {
        // Given
        User user = aUser();
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        ProjectConfiguration projectConfiguration = mock(ProjectConfiguration.class);
        when(projectRepository.getProjectConfigurationById("1234")).thenReturn(projectConfiguration);
        when(projectConfiguration.getAdmins()).thenReturn(Collections.singletonList(user).iterator());

        TestActorRef<Actor> subject = TestActorRef.create(actorSystem, ProjectUpdaterActor.props(projectRepository));

        // When
        Future<Object> future = Patterns.ask(subject, new ProjectUpdaterMessages.ProjectUpdateMsg(user, aProjectWithStacks(aBuildStack())), thirtySeconds);

        try {
            Object result = Await.result(future, twoSeconds);
            assertThat(result.getClass()).isEqualTo(ProjectUpdaterMessages.ProjectUpdateResultMsg.class);

            ProjectUpdaterMessages.ProjectUpdateResultMsg msg = (ProjectUpdaterMessages.ProjectUpdateResultMsg) result;
            assertThat(msg.getProject()).isNotNull();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // Then
        verify(projectRepository).updateProject(any(Project.class));
    }

    @AfterClass
    public static void tearDown() {
        JavaTestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

}