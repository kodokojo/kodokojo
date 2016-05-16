package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.model.BrickState;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.BrickType;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.StackConfiguration;
import io.kodokojo.service.BrickManager;
import io.kodokojo.brick.BrickAlreadyExist;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.ProjectConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;

public class BrickConfigurationStarterActor extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickConfigurationStarterActor.class);

    private final BrickManager brickManager;

    private final ConfigurationStore configurationStore;

    private final BrickUrlFactory brickUrlFactory;

    private final ActorRef stateListener;

    @Inject
    public BrickConfigurationStarterActor(BrickManager brickManager, ConfigurationStore configurationStore, BrickUrlFactory brickUrlFactory, ActorRef stateListener) {
        if (brickManager == null) {
            throw new IllegalArgumentException("brickManager must be defined.");
        }
        if (configurationStore == null) {
            throw new IllegalArgumentException("configurationStore must be defined.");
        }
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        if (stateListener == null) {
            throw new IllegalArgumentException("stateMsgEndpointCreator must be defined.");
        }
        this.brickManager = brickManager;
        this.brickUrlFactory = brickUrlFactory;
        this.configurationStore = configurationStore;
        this.stateListener = stateListener;

        receive(ReceiveBuilder.match(BrickStartContext.class, this::start)
                .matchAny(this::unhandled)
                .build()
        );
    }

    protected final void start(BrickStartContext brickStartContext) {
        BrickConfiguration brickConfiguration = brickStartContext.getBrickConfiguration();
        ProjectConfiguration projectConfiguration = brickStartContext.getProjectConfiguration();
        BrickType brickType = brickConfiguration.getType();
        String projectName = projectConfiguration.getName();
        String url = brickUrlFactory.forgeUrl(projectConfiguration,projectConfiguration.getDefaultStackConfiguration().getName(), brickConfiguration);
        String httpsUrl = "https://" + url;
        if (brickType.isRequiredHttpExposed()) {
            String brickTypeName = brickType.name().toLowerCase();
            SSLKeyPair projectCaSSL = brickStartContext.getProjectCaSSL();
            SSLKeyPair brickSslKeyPair = SSLUtils.createSSLKeyPair(url, projectCaSSL.getPrivateKey(), projectCaSSL.getPublicKey(), projectCaSSL.getCertificates());
            configurationStore.storeSSLKeys(projectName, brickTypeName, brickSslKeyPair);
        }

        try {
            generateMsgAndSend(brickStartContext, httpsUrl, BrickState.State.STARTING);


            Set<Service> services = brickManager.start(projectConfiguration, brickType);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} for project {} started : {}", brickType, projectName, StringUtils.join(services, ","));
            }
            generateMsgAndSend(brickStartContext, httpsUrl, BrickState.State.CONFIGURING);

            boolean configured = false;
            try {
                brickManager.configure(projectConfiguration, brickType);
                configured = true;
            } catch (ProjectConfigurationException e) {
                LOGGER.error("An error occure while trying to configure project {}", projectName, e);
                generateMsgAndSend(brickStartContext, httpsUrl, BrickState.State.ONFAILURE, e.getMessage());
            }

            if (configured) {
                generateMsgAndSend(brickStartContext, httpsUrl, BrickState.State.RUNNING);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("{} for project {} configured", brickType, projectName);
                }
            }
        } catch (BrickAlreadyExist brickAlreadyExist) {
            LOGGER.error("Brick {} already exist for project {}, not reconfigure it.", brickAlreadyExist.getBrickName(), brickAlreadyExist.getProjectName());
            generateMsgAndSend(brickStartContext, httpsUrl, BrickState.State.ALREADYEXIST);
        } catch (RuntimeException e) {
            LOGGER.error("An error occurred while trying to start brick {} for project {}.", brickType, projectName, e);
            generateMsgAndSend(brickStartContext, httpsUrl, BrickState.State.ONFAILURE, e.getMessage());
        }
    }


    private void generateMsgAndSend(BrickStartContext context,String url, BrickState.State state, String messageStr) {
        ProjectConfiguration projectConfiguration = context.getProjectConfiguration();
        StackConfiguration stackConfiguration = context.getStackConfiguration();
        BrickConfiguration brickConfiguration = context.getBrickConfiguration();
        BrickType brickType = brickConfiguration.getType();
        String brickName = brickConfiguration.getName();
        BrickState message = new BrickState(projectConfiguration.getIdentifier(),stackConfiguration.getName(),  brickType.name(), brickName, state, url, messageStr);
        stateListener.tell(message, self());
    }

    private void generateMsgAndSend(BrickStartContext context,String url, BrickState.State state) {
        generateMsgAndSend(context, url, state, null);
    }
}
