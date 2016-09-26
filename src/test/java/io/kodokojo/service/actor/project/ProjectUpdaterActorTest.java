package io.kodokojo.service.actor.project;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import io.kodokojo.model.*;
import io.kodokojo.service.actor.right.RightEndpointActor;
import io.kodokojo.service.repository.ProjectRepository;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProjectUpdaterActorTest {

    private static ActorSystem actorSystem;

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
    }

    @Test
    public void update_project_from_unknown_requester() {
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        TestActorRef<Actor> subject = TestActorRef.create(actorSystem, ProjectUpdaterActor.PROPS(projectRepository));

        HashSet<Stack> stacks = new HashSet<>();
        stacks.add(new Stack("build-A", StackType.BUILD, new HashSet<>()));


        Patterns.ask(subject, new ProjectUpdaterActor.ProjectUpdateMsg(null, new Project("1234", "test", new Date(), stacks)), 30000);

        verify(projectRepository).updateProject(any(Project.class));

    }

    @Test
    public void update_project_from_authorized_requester() {

        User user = new User("1234", "5678", "John Doe", "jdoe", "jdoe@inconnu.com", "jdoe4ever", "ssh key");

        ProjectRepository projectRepository = mock(ProjectRepository.class);

        ProjectConfiguration projectConfiguration = mock(ProjectConfiguration.class);

        when(projectRepository.getProjectConfigurationById("1234")).thenReturn(projectConfiguration);
        when(projectConfiguration.getAdmins()).thenReturn(Collections.singletonList(user).iterator());

        TestActorRef<Actor> subject = TestActorRef.create(actorSystem, ProjectUpdaterActor.PROPS(projectRepository));

        HashSet<Stack> stacks = new HashSet<>();
        stacks.add(new Stack("build-A", StackType.BUILD, new HashSet<>()));

        Future<Object> future = Patterns.ask(subject, new ProjectUpdaterActor.ProjectUpdateMsg(user, new Project("1234", "test", new Date(), stacks)), 30000);

        try {
            Object result = Await.result(future, FiniteDuration.create(2, TimeUnit.SECONDS));
            assertThat(result.getClass()).isEqualTo(ProjectUpdaterActor.ProjectUpdateResultMsg.class);
            ProjectUpdaterActor.ProjectUpdateResultMsg msg = (ProjectUpdaterActor.ProjectUpdateResultMsg) result;
            assertThat(msg.getProject()).isNotNull();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        verify(projectRepository).updateProject(any(Project.class));

    }

    @AfterClass
    public static void tearDown() {
        JavaTestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

}