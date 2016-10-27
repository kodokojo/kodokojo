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
package io.kodokojo.service.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import io.kodokojo.brick.*;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.model.Service;
import io.kodokojo.utils.RSAUtils;
import io.kodokojo.service.actor.project.BrickConfigurationStarterActor;
import io.kodokojo.service.ssl.SSLKeyPair;
import io.kodokojo.service.ssl.SSLUtils;
import io.kodokojo.model.*;
import io.kodokojo.service.BrickManager;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.ProjectConfigurationException;
import io.kodokojo.service.SSLCertificatProvider;
import org.apache.commons.collections4.IteratorUtils;
import org.assertj.core.api.Assertions;
import org.junit.*;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Ignore
public class BrickConfigurationConfigurationStarterActorTest {

    private static ActorSystem system;

    private BrickManager brickManager = mock(BrickManager.class);

    private BrickUrlFactory brickUrlFactory = new DefaultBrickUrlFactory("kodokojo.dev");


    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Before
    public void setupMock() {
        brickManager = mock(BrickManager.class);
    }

    @Test
    public void brick_configure_and_start_successfully() {
        BrickStartContext context = createBrickStartContext(new BrickConfiguration("test", BrickType.CI, "1.0", Collections.singleton(new PortDefinition(8080))));
        try {
            Set<Service> services = new HashSet<>();
            services.add(new Service("acme-ci", "192.168.1.22", new PortDefinition(42090)));
            when(brickManager.start(any(ProjectConfiguration.class), any(StackConfiguration.class), any(BrickConfiguration.class))).thenReturn(services);

            try {
                List<User> users = IteratorUtils.toList(context.getProjectConfiguration().getUsers());
                when(brickManager.configure(any(ProjectConfiguration.class), any(StackConfiguration.class), any(BrickConfiguration.class))).thenReturn(new BrickConfigurerData("projectTest", "build", "localhost", "kodokojo.dev", users, users));
            } catch (ProjectConfigurationException e) {
                fail(e.getMessage());
            }
        } catch (BrickAlreadyExist e) {
            fail(e.getMessage());
        }

        new JavaTestKit(system) {{

            JavaTestKit probe = new JavaTestKit(system);


            ActorRef ref = system.actorOf(BrickConfigurationStarterActor.PROPS(brickManager, brickUrlFactory));

            ref.tell(context, getRef());
            new AwaitAssert(duration("10000 millis")) {
                @Override
                protected void check() {

                    Object[] objects = probe.receiveN(3);
                    assertThat(objects.length).isEqualTo(3);
                    List<BrickStateEvent> brickStateEvents = Arrays.asList(objects).stream().map(o -> (BrickStateEvent) o).collect(Collectors.toList());
                    assertThat(brickStateEvents).extracting("state.name").contains(BrickStateEvent.State.CONFIGURING.name(), BrickStateEvent.State.STARTING.name(), BrickStateEvent.State.RUNNING.name());

                    try {
                        verify(brickManager).configure(any(ProjectConfiguration.class), any(StackConfiguration.class), any(BrickConfiguration.class));
                    } catch (ProjectConfigurationException e) {
                        fail(e.getMessage());
                    }
                }

            };
        }};
    }

    @Test
    public void brick_already_exist() {
        try {
            when(brickManager.start(any(ProjectConfiguration.class), any(StackConfiguration.class), any(BrickConfiguration.class))).thenThrow(new BrickAlreadyExist("test", "Acme"));
        } catch (BrickAlreadyExist e) {
            fail(e.getMessage());
        }

        new JavaTestKit(system) {{

            final Props props = BrickConfigurationStarterActor.PROPS(brickManager, brickUrlFactory);

            ActorRef ref = system.actorOf(props);
            BrickStartContext context = createBrickStartContext(new BrickConfiguration("test", BrickType.CI, "1.0", Collections.singleton(new PortDefinition(8080))));

            ref.tell(context, getRef());

            new AwaitAssert(duration("10 seconds")) {
                @Override
                protected void check() {
                    String[] states = new String[] {BrickStateEvent.State.STARTING.name(), BrickStateEvent.State.ALREADYEXIST.name()};
                    Object[] objects = receiveN(2);
                    assertThat(objects.length).isEqualTo(2);
                    List<BrickStateEvent> brickStateEvents = Arrays.asList(objects).stream().map(o -> (BrickStateEvent) o).collect(Collectors.toList());
                    assertThat(brickStateEvents).extracting("state.name").contains( states);

                }

            };
        }};
    }



    private BrickStartContext createBrickStartContext(BrickConfiguration brickConfiguration) {
        KeyPair keyPair = null;
        try {
            keyPair = RSAUtils.generateRsaKeyPair();
        } catch (NoSuchAlgorithmException e) {
            Assertions.fail(e.getMessage());
        }
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        SSLKeyPair sslKeyPair = SSLUtils.createSelfSignedSSLKeyPair("Test", (RSAPrivateKey) keyPair.getPrivate(), publicKey);
        User owner = new User("123456", "1é34", "Jean-Pascal THIERY", "jpthiery", "jpthiery@kodokojo.io", "jpthiery", RSAUtils.encodePublicKey(publicKey, "jpthiery@kodokojo.io"));
        Set<StackConfiguration> stackConfigurations = new HashSet<>();
        Set<BrickConfiguration> brickConfigurations = new HashSet<>();
        brickConfigurations.add(brickConfiguration);
        StackConfiguration stackConfiguration = new StackConfiguration("build-A", StackType.BUILD, brickConfigurations, 10022);
        stackConfigurations.add(stackConfiguration);
        List<User> users = Collections.singletonList(owner);
        UserService userService = new UserService("1244", "Acme-service", "Acme-service", "abcd",  (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        ProjectConfiguration projectConfiguration = new ProjectConfiguration("123456","7890", "Acme", userService, users, stackConfigurations, users);
        return new BrickStartContext(projectConfiguration, stackConfiguration, brickConfiguration);
    }


}
