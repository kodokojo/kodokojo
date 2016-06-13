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
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.brick.DefaultBrickUrlFactory;
import io.kodokojo.model.BrickState;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.*;
import io.kodokojo.service.BrickManager;
import io.kodokojo.brick.BrickAlreadyExist;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.ProjectConfigurationException;
import io.kodokojo.service.SSLCertificatProvider;
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
public class BrickConfigurationStarterActorTest {

    private static ActorSystem system;

    private BrickManager brickManager = mock(BrickManager.class);

    private ConfigurationStore configurationStore = mock(ConfigurationStore.class);

    private SSLCertificatProvider sslCertificatProvider = mock(SSLCertificatProvider.class);

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
        configurationStore = mock(ConfigurationStore.class);
    }

    @Test
    public void brick_configure_and_start_successfully() {
        try {
            Set<Service> services = new HashSet<>();
            services.add(new Service("acme-ci", "192.168.1.22", 42090));
            when(brickManager.start(any(ProjectConfiguration.class), eq(BrickType.CI))).thenReturn(services);
        } catch (BrickAlreadyExist e) {
            fail(e.getMessage());
        }

        new JavaTestKit(system) {{

            JavaTestKit probe = new JavaTestKit(system);

            final Props props = Props.create(BrickConfigurationStarterActor.class, brickManager, configurationStore, brickUrlFactory,sslCertificatProvider,  probe.getRef());


            ActorRef ref = system.actorOf(props);
            BrickStartContext context = createBrickStartContext(new BrickConfiguration(new Brick("test", BrickType.CI, "1.0")));

            ref.tell(context, getRef());
            new AwaitAssert(duration("10000 millis")) {
                @Override
                protected void check() {

                    Object[] objects = probe.receiveN(3);
                    assertThat(objects.length).isEqualTo(3);
                    List<BrickState> brickStates = Arrays.asList(objects).stream().map(o -> (BrickState) o).collect(Collectors.toList());
                    assertThat(brickStates).extracting("state.name").contains(BrickState.State.CONFIGURING.name(), BrickState.State.STARTING.name(), BrickState.State.RUNNING.name());

                    verify(configurationStore).storeSSLKeys(eq("Acme"), eq("ci"), any(SSLKeyPair.class));
                    try {
                        verify(brickManager).configure(any(ProjectConfiguration.class), eq(BrickType.CI));
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
            when(brickManager.start(any(ProjectConfiguration.class), eq(BrickType.CI))).thenThrow(new BrickAlreadyExist("test", "Acme"));
        } catch (BrickAlreadyExist e) {
            fail(e.getMessage());
        }

        new JavaTestKit(system) {{

            JavaTestKit probe = new JavaTestKit(system);

            final Props props = Props.create(BrickConfigurationStarterActor.class, brickManager, configurationStore, brickUrlFactory, sslCertificatProvider, probe.getRef());

            ActorRef ref = system.actorOf(props);
            BrickStartContext context = createBrickStartContext(new BrickConfiguration(new Brick("test", BrickType.CI, "1.0")));

            ref.tell(context, getRef());
            new AwaitAssert(duration("10 seconds")) {
                @Override
                protected void check() {
                    String[] states = new String[] {BrickState.State.STARTING.name(), BrickState.State.ALREADYEXIST.name()};
                    Object[] objects = probe.receiveN(2);
                    assertThat(objects.length).isEqualTo(2);
                    List<BrickState> brickStates = Arrays.asList(objects).stream().map(o -> (BrickState) o).collect(Collectors.toList());
                    assertThat(brickStates).extracting("state.name").contains( states);

                    verify(configurationStore).storeSSLKeys(eq("Acme"), eq("ci"), any(SSLKeyPair.class));
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
        User owner = new User("123456", "Jean-Pascal THIERY", "jpthiery", "jpthiery@kodokojo.io", "jpthiery", RSAUtils.encodePublicKey(publicKey, "jpthiery@kodokojo.io"));
        Set<StackConfiguration> stackConfigurations = new HashSet<>();
        Set<BrickConfiguration> brickConfigurations = new HashSet<>();
        brickConfigurations.add(brickConfiguration);
        StackConfiguration stackConfiguration = new StackConfiguration("build-A", StackType.BUILD, brickConfigurations, "127.0.0.1", 10022);
        stackConfigurations.add(stackConfiguration);
        List<User> users = Collections.singletonList(owner);
        ProjectConfiguration projectConfiguration = new ProjectConfiguration("123456","7890", "Acme", users, stackConfigurations, users);
        return new BrickStartContext(projectConfiguration, stackConfiguration, brickConfiguration, "kodokojo.dev", "127.0.0.1");
    }


}
