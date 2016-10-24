package io.kodokojo.test.utils;

import io.kodokojo.config.MarathonConfig;
import io.kodokojo.model.*;
import io.kodokojo.utils.RSAUtils;
import scala.concurrent.duration.FiniteDuration;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.fail;

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

    default PortDefinition aPortDefinition() {
        return new PortDefinition(8080);
    }

    default BrickConfiguration aBrickConfiguration() {
        BrickConfigurationBuilder builder = new BrickConfigurationBuilder();
        builder.setName("Gitlab")
                .setPortDefinitions(Collections.singleton(aPortDefinition()))
                .setType(BrickType.SCM)
                .setVersion("8.12");
        return builder.build();
    }

    default StackConfiguration aStackConfiguration() {
        StackConfigurationBuilder builder = new StackConfigurationBuilder();
        builder.setName("build-A")
                .setScmSshPort(10022)
                .setType(StackType.BUILD)
        .addBrickConfiguration(aBrickConfiguration());

        BrickConfigurationBuilder brickConfigurationBuilder = new BrickConfigurationBuilder();

        brickConfigurationBuilder.setName("Jenkins")
                .setPortDefinitions(Collections.singleton(aPortDefinition()))
                .setType(BrickType.CI)
                .setVersion("1.651.3");
        builder.addBrickConfiguration(brickConfigurationBuilder.build());
        brickConfigurationBuilder.setName("Nexus")
                .setPortDefinitions(Collections.singleton(aPortDefinition()))
                .setType(BrickType.REPOSITORY)
                .setVersion("2");
        builder.addBrickConfiguration(brickConfigurationBuilder.build());
        return builder.build();
    }

    default ProjectConfiguration aProjectConfiguration() {
        ProjectConfigurationBuilder builder = new ProjectConfigurationBuilder();
        builder.setName("myproject");
        builder.setEntityIdentifier("1234");
        builder.setIdentifier("5678");
        builder.setStackConfigurations(Collections.singleton(aStackConfiguration()));
        List<User> users = Collections.singletonList(anUser());
        builder.setAdmins(users);
        builder.setUsers(users);
        try {
            KeyPair keyPair = RSAUtils.generateRsaKeyPair();
            builder.setUserService(new UserService("userServiceId1234", "adminService", "admin", "admin", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic()));
            return builder.build();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        return null;
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
