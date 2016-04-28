package io.kodokojo.service.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.*;
import io.kodokojo.service.BrickManager;
import io.kodokojo.brick.BrickAlreadyExist;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.brick.BrickStateMsg;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.dns.DnsEntry;
import io.kodokojo.service.dns.DnsManager;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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

public class BrickConfigurationStarterActorTest {

    private static ActorSystem system;

    private BrickManager brickManager = mock(BrickManager.class);

    private ConfigurationStore configurationStore = mock(ConfigurationStore.class);

    private DnsManager dnsManager = mock(DnsManager.class);

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
        dnsManager = mock(DnsManager.class);
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

            final Props props = Props.create(BrickConfigurationStarterActor.class, brickManager, configurationStore, dnsManager, probe.getRef());


            ActorRef ref = system.actorOf(props);
            BrickStartContext context = createBrickStartContext(new BrickConfiguration(new Brick("test", BrickType.CI)));

            ref.tell(context, getRef());
            new AwaitAssert(duration("5 second")) {
                @Override
                protected void check() {
                    Object[] objects = probe.receiveN(3);
                    assertThat(objects.length).isEqualTo(3);
                    List<BrickStateMsg> brickStateMsgs = Arrays.asList(objects).stream().map(o -> (BrickStateMsg) o).collect(Collectors.toList());
                    assertThat(brickStateMsgs).extracting("state.name").contains(BrickStateMsg.State.CONFIGURING.name(), BrickStateMsg.State.STARTING.name(), BrickStateMsg.State.RUNNING.name());

                    ArgumentCaptor<DnsEntry> captor = ArgumentCaptor.forClass(DnsEntry.class);
                    verify(dnsManager).createOrUpdateDnsEntry(captor.capture());
                    DnsEntry dnsEntry = captor.getValue();
                    assertThat(dnsEntry.getType()).isEqualTo(DnsEntry.Type.A);
                    assertThat(dnsEntry.getName()).isEqualTo("ci.acme.kodokojo.dev");
                    assertThat(dnsEntry.getValue()).isEqualTo("127.0.0.1");

                    verify(configurationStore).storeSSLKeys(eq("Acme"), eq("ci"), any(SSLKeyPair.class));
                    verify(brickManager).configure(any(ProjectConfiguration.class), eq(BrickType.CI));
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

            final Props props = Props.create(BrickConfigurationStarterActor.class, brickManager, configurationStore, dnsManager, probe.getRef());

            ActorRef ref = system.actorOf(props);
            BrickStartContext context = createBrickStartContext(new BrickConfiguration(new Brick("test", BrickType.CI)));

            ref.tell(context, getRef());
            new AwaitAssert(duration("10 second")) {
                @Override
                protected void check() {
                    String[] states = new String[] {BrickStateMsg.State.STARTING.name(), BrickStateMsg.State.ALREADYEXIST.name()};
                    Object[] objects = probe.receiveN(2);
                    assertThat(objects.length).isEqualTo(2);
                    List<BrickStateMsg> brickStateMsgs = Arrays.asList(objects).stream().map(o -> (BrickStateMsg) o).collect(Collectors.toList());
                    assertThat(brickStateMsgs).extracting("state.name").contains( states);
                    ArgumentCaptor<DnsEntry> captor = ArgumentCaptor.forClass(DnsEntry.class);
                    verify(dnsManager).createOrUpdateDnsEntry(captor.capture());
                    DnsEntry dnsEntry = captor.getValue();
                    assertThat(dnsEntry.getType()).isEqualTo(DnsEntry.Type.A);
                    assertThat(dnsEntry.getName()).isEqualTo("ci.acme.kodokojo.dev");
                    assertThat(dnsEntry.getValue()).isEqualTo("127.0.0.1");

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
        stackConfigurations.add(new StackConfiguration("build-A", StackType.BUILD, brickConfigurations, "127.0.0.1", 10022));
        ProjectConfiguration prokectConfiguration = new ProjectConfiguration("123456", "Acme", owner, stackConfigurations, Collections.singletonList(owner));
        return new BrickStartContext(prokectConfiguration, brickConfiguration, "kodokojo.dev", sslKeyPair, "127.0.0.1");
    }


}
