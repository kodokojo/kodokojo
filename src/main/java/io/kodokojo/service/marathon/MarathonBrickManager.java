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
package io.kodokojo.service.marathon;

import io.kodokojo.brick.*;
import io.kodokojo.config.MarathonConfig;
import io.kodokojo.model.*;
import io.kodokojo.service.BrickManager;
import io.kodokojo.service.ProjectConfigurationException;
import io.kodokojo.service.repository.ProjectRepository;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
import retrofit2.Call;
import retrofit2.Retrofit;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

/*TODO Refacto :
    Move this class to Akka Actor
    Remove projectRepoitory
 */
public class MarathonBrickManager implements BrickManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonBrickManager.class);

    private static final Properties VE_PROPERTIES = new Properties();

    static {
        VE_PROPERTIES.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE_PROPERTIES.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        VE_PROPERTIES.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    }

    private final MarathonConfig marathonConfig;

    private final MarathonRestApi marathonRestApi;

    private final MarathonServiceLocator marathonServiceLocator;

    private final BrickConfigurerProvider brickConfigurerProvider;

    private final ProjectRepository projectRepository;

    private final boolean constrainByTypeAttribute;

    private final String domain;

    private final BrickUrlFactory brickUrlFactory;

    @Inject
    public MarathonBrickManager(MarathonConfig marathonConfig, MarathonServiceLocator marathonServiceLocator, BrickConfigurerProvider brickConfigurerProvider, ProjectRepository projectRepository, boolean constrainByTypeAttribute, String domain, BrickUrlFactory brickUrlFactory) {
        if (marathonConfig == null) {
            throw new IllegalArgumentException("marathonConfig must be defined.");
        }
        if (marathonServiceLocator == null) {
            throw new IllegalArgumentException("marathonServiceLocator must be defined.");
        }
        if (brickConfigurerProvider == null) {
            throw new IllegalArgumentException("brickConfigurerProvider must be defined.");
        }
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        if (isBlank(domain)) {
            throw new IllegalArgumentException("domain must be defined.");
        }
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        this.marathonConfig = marathonConfig;

        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(marathonConfig.url());
        if (StringUtils.isNotBlank(marathonConfig.login())) {
            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(chain -> {
                String basicAuthenticationValue = "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", marathonConfig.login(), marathonConfig.password()).getBytes());
                Request authenticateRequest = chain.request().newBuilder()
                        .addHeader("Authorization", basicAuthenticationValue)
                        .build();
                return chain.proceed(authenticateRequest);
            });
            builder.client(httpClientBuilder.build());
        }
        Retrofit adapter = builder.build();
        marathonRestApi = adapter.create(MarathonRestApi.class);
        this.marathonServiceLocator = marathonServiceLocator;
        this.brickConfigurerProvider = brickConfigurerProvider;
        this.projectRepository = projectRepository;
        this.constrainByTypeAttribute = constrainByTypeAttribute;
        this.domain = domain;
        this.brickUrlFactory = brickUrlFactory;
    }

    @Override
    public Set<Service> start(ProjectConfiguration projectConfiguration, StackConfiguration stackConfiguration, BrickConfiguration brickConfiguration) throws BrickAlreadyExist {
        requireNonNull(projectConfiguration, "projectConfiguration must be defined.");
        //requireNonNull(stackConfiguration, "stackConfiguration must be defined.");
        requireNonNull(brickConfiguration, "brickConfiguration must be defined.");

        String projectName = projectConfiguration.getName().toLowerCase();
        String type = brickConfiguration.getName().toLowerCase();

        String id = "/" + projectName.toLowerCase() + "/" + type;
        String body = provideStartAppBody(projectConfiguration, projectConfiguration.getDefaultStackConfiguration().getName(), brickConfiguration, id);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Push new Application configuration to Marathon :\n{}", body);
        }

        Set<Service> res = new HashSet<>();
        Call<Void> call = marathonRestApi.startApplication(body);
        try {
            call.execute();
            marathonServiceLocator.getService(type, projectName);
            boolean haveHttpService = getAnHttpService(res);
            // TODO remove this, listen Zookeeper instead
            int nbTry = 0;
            int maxNbTry = 10000;
            while (nbTry < maxNbTry && !haveHttpService) {
                nbTry++;
                res = marathonServiceLocator.getService(type, projectName);
                haveHttpService = getAnHttpService(res);
                if (!haveHttpService) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (IOException e) {
            throw new BrickAlreadyExist(e, brickConfiguration.getName(), projectName);
        }

        return res;
    }

    @Override
    public BrickConfigurerData configure(ProjectConfiguration projectConfiguration, StackConfiguration stackConfiguration, BrickConfiguration brickConfiguration) throws ProjectConfigurationException {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (stackConfiguration == null) {
            throw new IllegalArgumentException("stackConfiguration must be defined.");
        }
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }

        String name = projectConfiguration.getName().toLowerCase();
        BrickType brickType = brickConfiguration.getType();
        String type = brickType.name().toLowerCase();
        BrickConfigurer configurer = brickConfigurerProvider.provideFromBrick(brickConfiguration);
        if (configurer != null) {
            String brickName = brickConfiguration.getName().toLowerCase();
            LOGGER.debug("Lookup service {} for project {}.", brickName, name);
            Set<Service> services = marathonServiceLocator.getService(brickName, name);
            if (CollectionUtils.isNotEmpty(services)) {
                String entrypoint = getEntryPoint(services);
                if (StringUtils.isBlank(entrypoint)) {
                    LOGGER.error("Unable to find a valid entrypoint for brick '{}' on project {}", type, name);
                } else {
                    List<User> users = IteratorUtils.toList(projectConfiguration.getUsers());
                    try {
                        BrickConfigurerData brickConfigurerData = new BrickConfigurerData(projectConfiguration.getName(), projectConfiguration.getDefaultStackConfiguration().getName(), entrypoint, domain, IteratorUtils.toList(projectConfiguration.getAdmins()), users);
                        brickConfigurerData = configurer.configure(projectConfiguration, brickConfigurerData);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Receive following BrickConfigurerData when configure {} {} : {}", projectConfiguration.getName(), brickConfiguration.getName(), brickConfigurerData);
                        }
                        brickConfigurerData = configurer.addUsers(projectConfiguration, brickConfigurerData, users);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Adding users {} to brick {}", StringUtils.join(users, ","), brickType);
                            LOGGER.debug("Receive following BrickConfigurerData when add user on {} {} : {}", projectConfiguration.getName(), brickConfiguration.getName(), brickConfigurerData);

                        }
                        return brickConfigurerData;
                    } catch (BrickConfigurationException e) {
                        throw new ProjectConfigurationException("En error occur while trying to configure brick " + brickType.name() + " on project " + projectConfiguration.getName(), e);
                    }
                }
            } else {
                throw new RuntimeException("Unable to find a valid HTTP service for brick " + type + " on project " + name);
            }
        }
        throw new RuntimeException("Unable to find a BrickConfigurer for brick type " + brickType);
    }

    private String getEntryPoint(Set<Service> services) {
        String res = null;
        Iterator<Service> iterator = services.iterator();
        while (res == null && iterator.hasNext()) {
            Service service = iterator.next();
            if (service.getPortDefinition().getType() == PortDefinition.Type.HTTP || service.getPortDefinition().getType() == PortDefinition.Type.HTTPS) {
                res = "http://" + service.getHost() + ":" + service.getPortDefinition().getHostPort();
            }
        }
        return res;
    }

    @Override
    public boolean stop(BrickConfiguration brickDeploymentState) {
        if (brickDeploymentState == null) {
            throw new IllegalArgumentException("brickDeploymentState must be defined.");
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private String provideStartAppBody(ProjectConfiguration projectConfiguration, String stackName, BrickConfiguration brickConfiguration, String id) {
        VelocityEngine ve = new VelocityEngine();
        ve.init(VE_PROPERTIES);

        Template template = ve.getTemplate("marathon/" + brickConfiguration.getName().toLowerCase() + ".json.vm");

        VelocityContext context = new VelocityContext();
        context.put("ID", id);
        context.put("marathonUrl", marathonConfig.url());
        context.put("project", projectConfiguration);
        context.put("projectName", projectConfiguration.getName().toLowerCase());
        context.put("stack", projectConfiguration.getDefaultStackConfiguration());
        context.put("brick", brickConfiguration);
        context.put("brickUrl", brickUrlFactory.forgeUrl(projectConfiguration.getName(), stackName, brickConfiguration.getType().name(), brickConfiguration.getName()));
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
