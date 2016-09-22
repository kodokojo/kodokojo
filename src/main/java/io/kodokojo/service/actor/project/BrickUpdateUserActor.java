package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.brick.BrickConfigurer;
import io.kodokojo.brick.BrickConfigurerData;
import io.kodokojo.brick.BrickConfigurerProvider;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.StackConfiguration;
import io.kodokojo.model.User;
import org.apache.commons.collections4.IteratorUtils;

import java.util.List;

import static akka.event.Logging.getLogger;

public class BrickUpdateUserActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static final Props PROPS(ApplicationConfig applicationConfig, BrickUrlFactory brickUrlFactory, BrickConfigurerProvider brickConfigurerProvider) {
        if (applicationConfig == null) {
            throw new IllegalArgumentException("applicationConfig must be defined.");
        }
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        if (brickConfigurerProvider == null) {
            throw new IllegalArgumentException("brickConfigurerProvider must be defined.");
        }
        return Props.create(BrickUpdateUserActor.class, applicationConfig, brickUrlFactory, brickConfigurerProvider);
    }

    public BrickUpdateUserActor(ApplicationConfig applicationConfig, BrickUrlFactory brickUrlFactory, BrickConfigurerProvider brickConfigurerProvider) {
        receive(ReceiveBuilder.match(BrickUpdateUserMsg.class, msg -> {
            String url = "https://" + brickUrlFactory.forgeUrl(msg.projectConfiguration, msg.stackConfiguration.getName(), msg.brickConfiguration);
            BrickConfigurer brickConfigurer = brickConfigurerProvider.provideFromBrick(msg.brickConfiguration);
            BrickConfigurerData brickConfigurationData = new BrickConfigurerData(msg.projectConfiguration.getName(),
                    msg.stackConfiguration.getName(),
                    url,
                    applicationConfig.domain(),
                    IteratorUtils.toList(msg.projectConfiguration.getUsers()),
                    IteratorUtils.toList(msg.projectConfiguration.getAdmins()));
            brickConfigurationData.getContext().putAll(brickConfigurationData.getContext());
            switch (msg.typeChange) {
                case ADD:
                    brickConfigurationData = brickConfigurer.addUsers(brickConfigurationData, msg.users);
                    break;
                case REMOVE:
                    brickConfigurationData = brickConfigurer.removeUsers(brickConfigurationData, msg.users);
                    break;
            }
            sender().tell(new BrickUpdateUserResultMsg(msg, true), self());
            getContext().stop(self());
        }).matchAny(this::unhandled).build());
    }

    public static class BrickUpdateUserMsg {

        private final ProjectConfiguration projectConfiguration;

        private final StackConfiguration stackConfiguration;

        private final BrickConfiguration brickConfiguration;

        private final List<User> users;

        private final TypeChange typeChange;

        public BrickUpdateUserMsg(TypeChange typeChange, List<User> users, ProjectConfiguration projectConfiguration, StackConfiguration stackConfiguration, BrickConfiguration brickConfiguration) {
            this.users = users;
            this.typeChange = typeChange;
            if (projectConfiguration == null) {
                throw new IllegalArgumentException("projectConfiguration must be defined.");
            }
            if (stackConfiguration == null) {
                throw new IllegalArgumentException("stackConfiguration must be defined.");
            }
            if (brickConfiguration == null) {
                throw new IllegalArgumentException("brickConfiguration must be defined.");
            }
            this.projectConfiguration = projectConfiguration;
            this.stackConfiguration = stackConfiguration;
            this.brickConfiguration = brickConfiguration;
        }
    }

    public static class BrickUpdateUserResultMsg {

        private final BrickUpdateUserMsg request;

        private final boolean success;

        public BrickUpdateUserResultMsg(BrickUpdateUserMsg request, boolean success) {
            if (request == null) {
                throw new IllegalArgumentException("request must be defined.");
            }
            this.request = request;
            this.success = success;
        }

        public BrickUpdateUserMsg getRequest() {
            return request;
        }

        public boolean isSuccess() {
            return success;
        }
    }

}
