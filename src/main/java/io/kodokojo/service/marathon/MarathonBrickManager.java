package io.kodokojo.service.marathon;

import io.kodokojo.brick.*;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.servicelocator.marathon.MarathonServiceLocator;
import io.kodokojo.model.*;
import io.kodokojo.brick.BrickConfigurerData;
import io.kodokojo.service.BrickManager;
import io.kodokojo.service.ProjectConfigurationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.mime.TypedString;

import javax.inject.Inject;
import java.io.StringWriter;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

public class MarathonBrickManager implements BrickManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonBrickManager.class);

    private static final Properties VE_PROPERTIES = new Properties();

    static {
        VE_PROPERTIES.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE_PROPERTIES.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        VE_PROPERTIES.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    }

    private final String marathonUrl;

    private final MarathonRestApi marathonRestApi;

    private final MarathonServiceLocator marathonServiceLocator;

    private final BrickConfigurerProvider brickConfigurerProvider;

    private final boolean constrainByTypeAttribute;

    private final String domain;

    private final BrickUrlFactory brickUrlFactory;

    @Inject
    public MarathonBrickManager(String marathonUrl, MarathonServiceLocator marathonServiceLocator, BrickConfigurerProvider brickConfigurerProvider, boolean constrainByTypeAttribute, String domain, BrickUrlFactory brickUrlFactory) {
        if (isBlank(marathonUrl)) {
            throw new IllegalArgumentException("marathonUrl must be defined.");
        }
        if (marathonServiceLocator == null) {
            throw new IllegalArgumentException("marathonServiceLocator must be defined.");
        }
        if (brickConfigurerProvider == null) {
            throw new IllegalArgumentException("brickConfigurerProvider must be defined.");
        }
        if (isBlank(domain)) {
            throw new IllegalArgumentException("domain must be defined.");
        }
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        this.marathonUrl = marathonUrl;
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(marathonUrl).build();
        marathonRestApi = adapter.create(MarathonRestApi.class);
        this.marathonServiceLocator = marathonServiceLocator;
        this.brickConfigurerProvider = brickConfigurerProvider;
        this.constrainByTypeAttribute = constrainByTypeAttribute;
        this.domain = domain;
        this.brickUrlFactory = brickUrlFactory;
    }

    public MarathonBrickManager(String marathonUrl, MarathonServiceLocator marathonServiceLocator, BrickConfigurerProvider brickConfigurerProvider, String domain, BrickUrlFactory brickUrlFactory) {
        this(marathonUrl, marathonServiceLocator, brickConfigurerProvider, true, domain, brickUrlFactory);
    }


    @Override
    public Set<Service> start(ProjectConfiguration projectConfiguration, BrickType brickType) throws BrickAlreadyExist {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (brickType == null) {
            throw new IllegalArgumentException("brickType must be defined.");
        }

        Iterator<BrickConfiguration> brickConfigurations = projectConfiguration.getDefaultBrickConfigurations();
        BrickConfiguration brickConfiguration = getBrickConfiguration(brickType, brickConfigurations);

        if (brickConfiguration == null) {
            throw new IllegalStateException("Unable to find brickConfiguration for " + brickType);
        }
        String name = projectConfiguration.getName().toLowerCase();
        String type = brickType.name().toLowerCase();

        String id = "/" + name.toLowerCase() + "/" + brickConfiguration.getType().name().toLowerCase();
        String body = provideStartAppBody(projectConfiguration, projectConfiguration.getDefaultStackConfiguration().getName(), brickConfiguration, id);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Push new Application configuration to Marathon :\n{}", body);
        }
        TypedString input = new TypedString(body);
        try {
            marathonRestApi.startApplication(input);
        } catch (RetrofitError e) {
            if (e.getResponse().getStatus() == 409) {
                throw new BrickAlreadyExist(e,type, name);
            }
        }
        Set<Service> res = new HashSet<>();
        if (brickConfiguration.isWaitRunning()) {
            marathonServiceLocator.getService(type, name);
            boolean haveHttpService = getAnHttpService(res);
            // TODO remove this, listen the Marathon event bus instead
            int nbTry = 0;
            int maxNbTry = 10000;
            while (nbTry < maxNbTry && !haveHttpService) {
                nbTry++;
                res = marathonServiceLocator.getService(type, name);
                haveHttpService = getAnHttpService(res);
                if (!haveHttpService) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return res;
    }

    @Override
    public void configure(ProjectConfiguration projectConfiguration, BrickType brickType) throws ProjectConfigurationException {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (brickType == null) {
            throw new IllegalArgumentException("brickType must be defined.");
        }

        Iterator<BrickConfiguration> brickConfigurations = projectConfiguration.getDefaultBrickConfigurations();
        BrickConfiguration brickConfiguration = getBrickConfiguration(brickType, brickConfigurations);

        if (brickConfiguration == null) {
            throw new IllegalStateException("Unable to find brickConfiguration for " + brickType);
        }
        String name = projectConfiguration.getName().toLowerCase();
        String type = brickType.name().toLowerCase();
        BrickConfigurer configurer = brickConfigurerProvider.provideFromBrick(brickConfiguration.getBrick());
        if (configurer != null) {
            Set<Service> services = marathonServiceLocator.getService(type, name);
            if (CollectionUtils.isNotEmpty(services)) {
                String entrypoint = getEntryPoint(services);
                if (StringUtils.isBlank(entrypoint)) {
                    LOGGER.error("Unable to find a valid entrypoint for brick '{}' on project {}", type, name);
                } else {
                    List<User> users = IteratorUtils.toList(projectConfiguration.getUsers());
                    try {
                        BrickConfigurerData brickConfigurerData = configurer.configure(new BrickConfigurerData(projectConfiguration.getName(), projectConfiguration.getDefaultStackConfiguration().getName(), entrypoint, domain, IteratorUtils.toList(projectConfiguration.getAdmins()), users));
                        configurer.addUsers(brickConfigurerData, users);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Adding users {} to brick {}", StringUtils.join(users, ","), brickType);
                        }
                    } catch (BrickConfigurationException e) {
                        throw  new ProjectConfigurationException("En error occur while trying to configure brick " + brickType.name() + " on project " +projectConfiguration.getName(), e);
                    }
                }
            } else {
                LOGGER.error("Unable to find http service for brick '{}' on project {}.", type, name);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Configurer Not defined for brick {}", brickType);
        }

    }

    private String getEntryPoint(Set<Service> services) {
        String res = null;
        Iterator<Service> iterator = services.iterator();
        while (res == null && iterator.hasNext()) {
            Service service = iterator.next();
            String name = service.getName();
            if (!name.endsWith("-22")) {
                res = "http" + (name.endsWith("-443") ? "s" : "") + "://" + service.getHost() + ":" + service.getPort();
            }
        }
        return res;
    }

    @Override
    public boolean stop(BrickDeploymentState brickDeploymentState) {
        if (brickDeploymentState == null) {
            throw new IllegalArgumentException("brickDeploymentState must be defined.");
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private BrickConfiguration getBrickConfiguration(BrickType brickType, Iterator<BrickConfiguration> iterator) {
        BrickConfiguration brickConfiguration = null;
        while (brickConfiguration == null && iterator.hasNext()) {
            BrickConfiguration configuration = iterator.next();
            if (configuration.getType().equals(brickType)) {
                brickConfiguration = configuration;
            }
        }
        return brickConfiguration;
    }

    private String provideStartAppBody(ProjectConfiguration projectConfiguration, String stackName, BrickConfiguration brickConfiguration, String id) {
        VelocityEngine ve = new VelocityEngine();
        ve.init(VE_PROPERTIES);

        Template template = ve.getTemplate("marathon/" + brickConfiguration.getBrick().getName().toLowerCase() + ".json.vm");

        VelocityContext context = new VelocityContext();
        context.put("ID", id);
        context.put("marathonUrl", marathonUrl);
        context.put("project", projectConfiguration);
        context.put("projectName", projectConfiguration.getName().toLowerCase());
        context.put("stack", projectConfiguration.getDefaultStackConfiguration());
        context.put("brick", brickConfiguration);
        context.put("brickUrl", brickUrlFactory.forgeUrl(projectConfiguration.getName(),stackName, brickConfiguration.getType().name()));
        context.put("constrainByTypeAttribute", this.constrainByTypeAttribute);
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString();
    }

    private boolean getAnHttpService(Set<Service> services) {
        boolean res = false;
        Iterator<Service> iterator = services.iterator();
        while (!res && iterator.hasNext()) {
            Service service = iterator.next();
            String name = service.getName();
            res = !name.endsWith("-22");
        }
        return res;
    }

}
