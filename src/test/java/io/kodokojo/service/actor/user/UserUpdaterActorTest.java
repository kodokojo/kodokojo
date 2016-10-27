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
package io.kodokojo.service.actor.user;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import akka.util.Timeout;
import io.kodokojo.model.User;
import io.kodokojo.service.repository.UserRepository;
import io.kodokojo.test.utils.DataBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UserUpdaterActorTest implements DataBuilder {


    private static ActorSystem actorSystem;

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
    }


    @Test
    public void update_user_and_request_brick_changed() {

        // given
        User requester = anUser();

        UserRepository userRepository = mock(UserRepository.class);
        TestActorRef<Actor> subject = TestActorRef.create(actorSystem, UserUpdaterActor.PROPS(userRepository));
        //actorSystem.actorOf(Props.create())

        // when
        Future<Object> future = ask(subject, new UserMessage.UserUpdateMessage(requester, requester, "newPassword", "newwSSH", "Marcel", "Dupond", "marcel@dupond.com"), Timeout.apply(1, TimeUnit.SECONDS));
        try {
            Object result = Await.result(future, Duration.create(1, TimeUnit.SECONDS));
            assertThat(result).isNotNull();
            assertThat(result.getClass()).isEqualTo(UserMessage.UserUpdateMessageResult.class);
            UserMessage.UserUpdateMessageResult msgResult = (UserMessage.UserUpdateMessageResult) result;
            assertThat(msgResult.isSuccess()).isTrue();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).updateUser(captor.capture());
        User userCaptured = captor.getValue();

        assertThat(userCaptured).isNotNull();
        assertThat(userCaptured.getPassword()).isEqualTo("newPassword");
        assertThat(userCaptured.getSshPublicKey()).isEqualTo("newwSSH");
    }

    @AfterClass
    public static void tearDown() {
        JavaTestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

}