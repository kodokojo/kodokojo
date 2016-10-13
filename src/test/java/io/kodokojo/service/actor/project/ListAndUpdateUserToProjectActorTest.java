package io.kodokojo.service.actor.project;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import akka.util.Timeout;
import io.kodokojo.model.*;
import io.kodokojo.service.repository.ProjectRepository;
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

public class ListAndUpdateUserToProjectActorTest {


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

        UserBuilder builder = new UserBuilder();
        builder.setFirstName("charles");
        builder.setLastName("dupond");
        builder.setEmail("cdupond@sample.com");
        builder.setUsername("cdupond");
        builder.setEntityIdentifier("1234");
        builder.setIdentifier("5678");
        builder.setPassword("oldPawword");
        builder.setSshPublicKey("oldSshPublicKey");
        User requester = builder.build();

        ProjectRepository projectRepository = mock(ProjectRepository.class);
        TestActorRef<Actor> subject = TestActorRef.create(actorSystem, ListAndUpdateUserToProjectActor.PROPS(projectRepository));

        BrickConfigurationBuilder brickConfigurationBuilder = new BrickConfigurationBuilder();
        brickConfigurationBuilder.setVersion("1.0.0");
        Set<PortDefinition> portDefinitions = Collections.singleton(new PortDefinition(8080));
        brickConfigurationBuilder.setPortDefinitions(portDefinitions);

        StackConfigurationBuilder stackConfigurationBuilder = new StackConfigurationBuilder();
        brickConfigurationBuilder.setName("brick1");
        brickConfigurationBuilder.setType(BrickType.CI);
        stackConfigurationBuilder.addBrickConfiguration(brickConfigurationBuilder.build());
        brickConfigurationBuilder.setName("brick2");
        brickConfigurationBuilder.setType(BrickType.SCM);
        stackConfigurationBuilder.setName("Build-A");
        stackConfigurationBuilder.setType(StackType.BUILD);
        stackConfigurationBuilder.addBrickConfiguration(brickConfigurationBuilder.build());

        ProjectConfigurationBuilder projectConfigurationBuilder = new ProjectConfigurationBuilder();
        projectConfigurationBuilder.setUserService(mock(UserService.class));
        projectConfigurationBuilder.setEntityIdentifier("my@entity.com");
        projectConfigurationBuilder.setName("myProject");
        projectConfigurationBuilder.setIdentifier("007");
        List<User> users = Collections.singletonList(requester);

        projectConfigurationBuilder.setAdmins(users);
        projectConfigurationBuilder.setUsers(users);
        projectConfigurationBuilder.addStackConfiguration(stackConfigurationBuilder.build());

        // when

        Set<String> projectIds = Collections.singleton("9000");
        when(projectRepository.getProjectConfigIdsByUserIdentifier("5678")).thenReturn(projectIds);

        when(projectRepository.getProjectConfigurationById("9000")).thenReturn(projectConfigurationBuilder.build());

        Future<Object> future = ask(subject, new ProjectUpdaterMessages.ListAndUpdateUserToProjectMsg(requester, requester), Timeout.apply(1, TimeUnit.SECONDS));
        try {
            Object result = Await.result(future, Duration.apply(1, TimeUnit.SECONDS));
            assertThat(result).isNotNull();
            assertThat(result.getClass()).isEqualTo(ProjectUpdaterMessages.ListAndUpdateUserToProjectResultMsg.class);
            TestEndpointActor underlyingActor = (TestEndpointActor) endpointRef.underlyingActor();
            Set<Object> messages = underlyingActor.getMessages();
            assertThat(messages.size()).isEqualTo(2);
            messages.stream().forEach(m -> {
                assertThat(m.getClass()).isEqualTo(BrickUpdateUserActor.BrickUpdateUserMsg.class);
            });

        } catch (Exception e) {
            fail(e.getMessage());
        }


        //then
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