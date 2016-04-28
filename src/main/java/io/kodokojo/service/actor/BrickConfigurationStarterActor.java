package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.commons.utils.ssl.SSLUtils;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.BrickType;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.service.BrickManager;
import io.kodokojo.brick.BrickAlreadyExist;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.brick.BrickStateMsg;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.dns.DnsEntry;
import io.kodokojo.service.dns.DnsManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;

public class BrickConfigurationStarterActor extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickConfigurationStarterActor.class);

    private final BrickManager brickManager;

    private final ConfigurationStore configurationStore;

    private final DnsManager dnsManager;

    private final ActorRef stateListener;

    @Inject
    public BrickConfigurationStarterActor(BrickManager brickManager, ConfigurationStore configurationStore, DnsManager dnsManager, ActorRef stateListener) {
        if (brickManager == null) {
            throw new IllegalArgumentException("brickManager must be defined.");
        }
        if (configurationStore == null) {
            throw new IllegalArgumentException("configurationStore must be defined.");
        }
        if (dnsManager == null) {
            throw new IllegalArgumentException("dnsManager must be defined.");
        }
        if (stateListener == null) {
            throw new IllegalArgumentException("stateMsgEndpointCreator must be defined.");
        }
        this.brickManager = brickManager;
        this.configurationStore = configurationStore;
        this.dnsManager = dnsManager;
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
        String projectDomain = (projectConfiguration.getName() + "." + brickStartContext.getDomaine()).toLowerCase();
        if (brickType.isRequiredHttpExposed()) {
            String brickTypeName = brickType.name().toLowerCase();
            String brickDomainName = brickTypeName + "." + projectDomain;
            String lbIp = brickStartContext.getLbIp();
            dnsManager.createOrUpdateDnsEntry(new DnsEntry(brickDomainName, DnsEntry.Type.A, lbIp));
            SSLKeyPair projectCaSSL = brickStartContext.getProjectCaSSL();
            SSLKeyPair brickSslKeyPair = SSLUtils.createSSLKeyPair(brickDomainName, projectCaSSL.getPrivateKey(), projectCaSSL.getPublicKey(), projectCaSSL.getCertificates());
            configurationStore.storeSSLKeys(projectName, brickTypeName, brickSslKeyPair);
        }

        try {
            BrickStateMsg message = new BrickStateMsg(projectConfiguration.getIdentifier(), brickType.name(), brickConfiguration.getName(), BrickStateMsg.State.STARTING);
            stateListener.tell(message, self());

            Set<Service> services = brickManager.start(projectConfiguration, brickType);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} for project {} started : {}", brickType, projectName, StringUtils.join(services, ","));
            }

            message = new BrickStateMsg(projectConfiguration.getIdentifier(), brickType.name(), brickConfiguration.getName(), BrickStateMsg.State.CONFIGURING);
            stateListener.tell(message, self());

            brickManager.configure(projectConfiguration, brickType);
            message = new BrickStateMsg(projectConfiguration.getIdentifier(), brickType.name(), brickConfiguration.getName(), BrickStateMsg.State.RUNNING);
            stateListener.tell(message, self());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} for project {} configured", brickType, projectName);
            }
        } catch (BrickAlreadyExist brickAlreadyExist) {
            LOGGER.error("Brick {} already exist for project {}, not reconfigure it.", brickAlreadyExist.getBrickName(), brickAlreadyExist.getProjectName());
            BrickStateMsg message = new BrickStateMsg(projectConfiguration.getIdentifier(), brickType.name(), brickConfiguration.getName(), BrickStateMsg.State.ALREADYEXIST);
            stateListener.tell(message, self());
        }
    }
}
