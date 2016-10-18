/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.brick.*;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.model.*;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static akka.event.Logging.getLogger;
import static java.util.Objects.requireNonNull;

public class BrickUpdateUserActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    private final ApplicationConfig applicationConfig;

    private final BrickUrlFactory brickUrlFactory;

    private final BrickConfigurerProvider brickConfigurerProvider;

    public BrickUpdateUserActor(ApplicationConfig applicationConfig, BrickUrlFactory brickUrlFactory, BrickConfigurerProvider brickConfigurerProvider) {
        this.applicationConfig = applicationConfig;
        this.brickUrlFactory = brickUrlFactory;
        this.brickConfigurerProvider = brickConfigurerProvider;

        receive(ReceiveBuilder
                .match(BrickUpdateUserMsg.class, this::onBrickUpdateUser)
                .matchAny(this::unhandled).build());

    }

    public static Props PROPS(ApplicationConfig applicationConfig, BrickUrlFactory brickUrlFactory, BrickConfigurerProvider brickConfigurerProvider) {
        requireNonNull(applicationConfig, "applicationConfig must be defined.");
        requireNonNull(brickUrlFactory, "brickUrlFactory must be defined.");
        requireNonNull(brickConfigurerProvider, "brickConfigurerProvider must be defined.");
        return Props.create(BrickUpdateUserActor.class, applicationConfig, brickUrlFactory, brickConfigurerProvider);
    }

    private void onBrickUpdateUser(BrickUpdateUserMsg msg) throws BrickConfigurationException {

        String url = "https://" + brickUrlFactory.forgeUrl(msg.projectConfiguration, msg.stackConfiguration.getName(), msg.brickConfiguration);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to {} user {} on brick url {}.",
                    msg.typeChange.toString(),
                    StringUtils.join(msg.users.stream()
                                    .map(entry -> (entry.getNewData() != null ? entry.getNewData().getUsername() : entry.getOldData().getUsername())).collect(Collectors.toList()),
                            ", "),
                    url
            );
        }
        BrickConfigurer brickConfigurer = brickConfigurerProvider.provideFromBrick(msg.brickConfiguration);
        BrickConfigurerData brickConfigurationData = new BrickConfigurerData(msg.projectConfiguration.getName(),
                msg.stackConfiguration.getName(),
                url,
                applicationConfig.domain(),
                IteratorUtils.toList(msg.projectConfiguration.getUsers()),
                IteratorUtils.toList(msg.projectConfiguration.getAdmins()));
        brickConfigurationData.getContext().putAll(msg.brickConfiguration.getProperties());
        LOGGER.debug("brickConfigurationData context: {}", brickConfigurationData.getContext());
        switch (msg.typeChange) {
            case ADD:
                brickConfigurationData = brickConfigurer.addUsers(msg.projectConfiguration, brickConfigurationData, msg.users.stream().map(UpdateData::getNewData).collect(Collectors.toList()));
                break;
            case UPDATE:
                brickConfigurationData = brickConfigurer.updateUsers(msg.projectConfiguration, brickConfigurationData, msg.users);
                break;
            case REMOVE:
                brickConfigurationData = brickConfigurer.removeUsers(msg.projectConfiguration, brickConfigurationData, msg.users.stream().map(UpdateData::getOldData).collect(Collectors.toList()));
                break;
        }
        sender().tell(new BrickUpdateUserResultMsg(msg, true), self());
        getContext().stop(self());
    }

    public static class BrickUpdateUserMsg {

        private final ProjectConfiguration projectConfiguration;

        private final StackConfiguration stackConfiguration;

        private final BrickConfiguration brickConfiguration;

        private final List<UpdateData<User>> users;

        private final TypeChange typeChange;

        public BrickUpdateUserMsg(TypeChange typeChange, List<UpdateData<User>> users, ProjectConfiguration projectConfiguration, StackConfiguration stackConfiguration, BrickConfiguration brickConfiguration) {
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
