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

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.kodokojo.brick.BrickConfigurerData;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.model.*;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.ssl.SSLKeyPair;
import io.kodokojo.service.BrickManager;
import io.kodokojo.brick.BrickAlreadyExist;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.ProjectConfigurationException;
import io.kodokojo.service.SSLCertificatProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static akka.event.Logging.getLogger;

//  TODO Refacto
public class BrickConfigurationStarterActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(BrickManager brickManager, ConfigurationStore configurationStore, BrickUrlFactory brickUrlFactory, SSLCertificatProvider sslCertificatProvider) {
        return Props.create(BrickConfigurationStarterActor.class, brickManager, configurationStore, brickUrlFactory, sslCertificatProvider);
    }

    private final BrickManager brickManager;

    private final ConfigurationStore configurationStore;

    private final BrickUrlFactory brickUrlFactory;

    private final SSLCertificatProvider sslCertificatProvider;

    @Inject
    public BrickConfigurationStarterActor(BrickManager brickManager, ConfigurationStore configurationStore, BrickUrlFactory brickUrlFactory, SSLCertificatProvider sslCertificatProvider) {
        if (brickManager == null) {
            throw new IllegalArgumentException("brickManager must be defined.");
        }
        if (configurationStore == null) {
            throw new IllegalArgumentException("configurationStore must be defined.");
        }
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        if (sslCertificatProvider == null) {
            throw new IllegalArgumentException("sslCertificatProvider must be defined.");
        }
        this.brickManager = brickManager;
        this.brickUrlFactory = brickUrlFactory;
        this.configurationStore = configurationStore;
        this.sslCertificatProvider = sslCertificatProvider;

        receive(ReceiveBuilder.match(BrickStartContext.class, msg -> this.start(msg, sender()))
                .matchAny(this::unhandled)
                .build()
        );

    }

    protected final void start(BrickStartContext brickStartContext, ActorRef sender) {
        ProjectConfiguration projectConfiguration = brickStartContext.getProjectConfiguration();
        StackConfiguration stackConfiguration = brickStartContext.getStackConfiguration();
        BrickConfiguration brickConfiguration = brickStartContext.getBrickConfiguration();
        BrickType brickType = brickConfiguration.getType();
        String projectName = projectConfiguration.getName();
        String url = brickUrlFactory.forgeUrl(projectConfiguration,projectConfiguration.getDefaultStackConfiguration().getName(), brickConfiguration);
        String httpsUrl = "https://" + url;
        if (brickType.isRequiredHttpExposed()) {
            SSLKeyPair brickSslKeyPair = sslCertificatProvider.provideCertificat(projectName, brickStartContext.getStackConfiguration().getName(), brickConfiguration);
            configurationStore.storeSSLKeys(projectName, brickStartContext.getBrickConfiguration().getName().toLowerCase(), brickSslKeyPair);
        }

        try {
            generateMsgAndSend(brickStartContext, httpsUrl, null, BrickStateEvent.State.STARTING);


            Set<Service> services = brickManager.start(projectConfiguration, stackConfiguration, brickConfiguration);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} for project {} started : {}", brickType, projectName, StringUtils.join(services, ","));
            }
            generateMsgAndSend(brickStartContext, httpsUrl, BrickStateEvent.State.STARTING, BrickStateEvent.State.CONFIGURING);

            BrickConfigurerData brickConfigurerData = null;
            try {
                brickConfigurerData = brickManager.configure(projectConfiguration,stackConfiguration, brickConfiguration);

            } catch (ProjectConfigurationException e) {
                LOGGER.error("An error occure while trying to configure project {}", projectName, e);
                generateMsgAndSend(brickStartContext, httpsUrl, null, BrickStateEvent.State.ONFAILURE, e.getMessage());
            }

            if (brickConfigurerData != null) {
                BrickConfigurationBuilder brickConfigurationBuilder = new BrickConfigurationBuilder(brickConfiguration);
                brickConfigurationBuilder.setProperties(brickConfigurerData.getContext());

                stackConfiguration.getBrickConfigurations().remove(brickConfiguration);
                BrickConfiguration brickConfigurationToSave = brickConfigurationBuilder.build();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Saving brick context {}", brickConfigurationToSave.getProperties());
                }
                stackConfiguration.getBrickConfigurations().add(brickConfigurationToSave);

                ProjectConfigurationBuilder builder = new ProjectConfigurationBuilder(projectConfiguration);

                Future<Object> future = Patterns.ask(getContext().actorFor(EndpointActor.ACTOR_PATH), new ProjectConfigurationUpdaterActor.ProjectConfigurationUpdaterMsg(null, builder.build()), Timeout.apply(10, TimeUnit.SECONDS));
                try {
                    Await.result(future, Duration.apply(10, TimeUnit.SECONDS));
                    generateMsgAndSend(brickStartContext, httpsUrl, BrickStateEvent.State.CONFIGURING, BrickStateEvent.State.RUNNING);
                    BrickStateEvent brickStateEvent = new BrickStateEvent(projectConfiguration.getIdentifier(), brickStartContext.getStackConfiguration().getName(), brickType.name(), brickStartContext.getBrickConfiguration().getName(), null, BrickStateEvent.State.RUNNING, url, "", brickConfiguration.getVersion());
                    sender.tell(brickStateEvent, self());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("{} for project {} configured", brickType, projectName);
                    }
                } catch (Exception e) {
                    LOGGER.error("Unable to update projectConfiguration {}", projectConfiguration, e);
                }
            }
        } catch (BrickAlreadyExist brickAlreadyExist) {
            LOGGER.error("BrickConfiguration {} already exist for project {}, not reconfigure it.", brickAlreadyExist.getBrickName(), brickAlreadyExist.getProjectName());
            generateMsgAndSend(brickStartContext, httpsUrl, null, BrickStateEvent.State.ALREADYEXIST);
            sender.tell(Futures.failed(brickAlreadyExist), self());
        } catch (RuntimeException e) {
            LOGGER.error("An error occurred while trying to start brick {} for project {}.", brickType, projectName, e);
            generateMsgAndSend(brickStartContext, httpsUrl, null, BrickStateEvent.State.ONFAILURE, e.getMessage());
            sender.tell(Futures.failed(e), self());
        }
        getContext().stop(self());

    }

    private void generateMsgAndSend(BrickStartContext context, String url, BrickStateEvent.State oldState, BrickStateEvent.State newState, String messageStr) {
        ProjectConfiguration projectConfiguration = context.getProjectConfiguration();
        StackConfiguration stackConfiguration = context.getStackConfiguration();
        BrickConfiguration brickConfiguration = context.getBrickConfiguration();
        BrickType brickType = brickConfiguration.getType();
        String brickName = brickConfiguration.getName();
        BrickStateEvent message = new BrickStateEvent(projectConfiguration.getIdentifier(),stackConfiguration.getName(),  brickType.name(), brickName, oldState, newState, url, messageStr, brickConfiguration.getVersion());
        getContext().actorFor(EndpointActor.ACTOR_PATH).tell(message, self());
    }

    private void generateMsgAndSend(BrickStartContext context,String url, BrickStateEvent.State oldState, BrickStateEvent.State newState) {
        generateMsgAndSend(context, url, oldState, newState, "");
    }
}
