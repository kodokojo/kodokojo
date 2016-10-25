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
package io.kodokojo.brick.jenkins;


import io.kodokojo.brick.BrickConfigurer;
import io.kodokojo.brick.BrickConfigurerData;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.UpdateData;
import io.kodokojo.model.User;
import io.kodokojo.model.UserService;
import io.kodokojo.utils.RSAUtils;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class JenkinsConfigurer implements BrickConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsConfigurer.class);

    private static final Properties VE_PROPERTIES = new Properties();

    private static final String SCRIPT_URL_SUFFIX = "/scriptText";

    private static final String HEALTH_URL_SUFFIX = "/cli";

    private static final String INIT_JENKINS_GROOVY_VM = "init_jenkins.groovy.vm";

    private static final String ADD_OR_UPDATE_USER_JENKINS_GROOVY_VM = "add_user_jenkins.groovy.vm";

    private static final String DELETE_USER_JENKINS_GROOVY_VM = "delete_user_jenkins.groovy.vm";

    private static final String USERS_KEY = "users";

    private static final String SCRIPT_KEY = "script";

    static {
        VE_PROPERTIES.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE_PROPERTIES.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        VE_PROPERTIES.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    }

    private final OkHttpClient httpClient;

    @Inject
    public JenkinsConfigurer(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        if (httpClient == null) {
            throw new IllegalArgumentException("httpClient must be defined.");
        }
    }

    @Override
    public BrickConfigurerData configure(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData) {

        VelocityContext context = new VelocityContext();
        List<User> users = new ArrayList<>();
        UserService userService = projectConfiguration.getUserService();
        String email = userService.getLogin() + "@kodokojo.io";
        users.add(new User("1234", "1234", userService.getName(), userService.getLogin(), email, userService.getPassword(), RSAUtils.encodePublicKey(userService.getPublicKey(), email)));
        context.put(USERS_KEY, users);
        String templatePath = INIT_JENKINS_GROOVY_VM;

        BrickConfigurerData initBrickConfigurerData = executeGroovyScript(userService, brickConfigurerData, context, templatePath, false);

        String baseUrl = brickConfigurerData.getEntrypoint();
        int nbTry = 0;
        boolean added;
        do {
            added = checkUserExist(baseUrl, userService.getLogin(), userService.getPassword());
            if (!added) {
                LOGGER.debug("Service user not added to {}. try number {}.", baseUrl, nbTry);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            nbTry++;
        } while (!added && nbTry < 9000);

        return initBrickConfigurerData;
    }

    private boolean checkUserExist(String baseUrl, String login, String password) {
        String url = baseUrl + "/me/configure";
        Request.Builder builder = new Request.Builder().get().url(url);
        addAuthen(builder, login, password);
        Response response = null;
        try {
            response = httpClient.newCall(builder.build()).execute();
            return response.code() == 200;
        } catch (IOException e) {
            LOGGER.error("Unable to request on {}", url, e);
            return false;
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }


    @Override
    public BrickConfigurerData addUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData, List<User> users) {
        VelocityContext context = new VelocityContext();
        context.put(USERS_KEY, brickConfigurerData.getUsers());

        String templatePath = ADD_OR_UPDATE_USER_JENKINS_GROOVY_VM;
        UserService userService = projectConfiguration.getUserService();
        BrickConfigurerData res = executeGroovyScript(userService, brickConfigurerData, context, templatePath);
        checkUserExist(getEntryPoint(brickConfigurerData), userService.getLogin(), userService.getPassword());
        return res;

    }

    @Override
    public BrickConfigurerData updateUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData, List<UpdateData<User>> users) {
        return addUsers(projectConfiguration, brickConfigurerData, users.stream().map(UpdateData::getNewData).collect(Collectors.toList()));
    }

    @Override
    public BrickConfigurerData removeUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData, List<User> users) {
        VelocityContext context = new VelocityContext();
        context.put(USERS_KEY, brickConfigurerData.getUsers());
        String templatePath = DELETE_USER_JENKINS_GROOVY_VM;
        return executeGroovyScript(projectConfiguration.getUserService(), brickConfigurerData, context, templatePath);
    }

    private BrickConfigurerData executeGroovyScript(UserService admin, BrickConfigurerData brickConfigurerData, VelocityContext context, String templatePath) {
        return executeGroovyScript(admin, brickConfigurerData, context, templatePath, true);
    }

    private BrickConfigurerData executeGroovyScript(UserService admin, BrickConfigurerData brickConfigurerData, VelocityContext context, String templatePath, boolean authen) {
        String url = getEntryPoint(brickConfigurerData);

        Response response = null;
        try {
            VelocityEngine ve = new VelocityEngine();
            ve.init(VE_PROPERTIES);

            Template template = ve.getTemplate(templatePath);

            StringWriter sw = new StringWriter();
            template.merge(context, sw);
            String script = sw.toString();

            RequestBody body = new FormBody.Builder().add(SCRIPT_KEY, script).build();

            Request.Builder builder = new Request.Builder().url(url).post(body);

            if (authen) {
                String login = admin.getLogin();
                String password = admin.getPassword();
                addAuthen(builder, login, password);
            }
            Request request = builder.build();
            int nbTry = 0;
            boolean success = false;
            do {
                response = httpClient.newCall(request).execute();
                String bodyResponse = response.body().string();
                success = response.code() >= 200 && response.code() < 300;
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Jenkins response: \n", bodyResponse);
                }
                nbTry++;
                if (!success) {
                    LOGGER.debug("Request {} FAILED, try number {}", request.toString(), nbTry);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } while (!success && nbTry < 9000 && !Thread.currentThread().isInterrupted());
            if (response.code() >= 200 && response.code() < 300) {
                return brickConfigurerData;
            }
            throw new RuntimeException("Unable to configure Jenkins " + brickConfigurerData.getEntrypoint() + ". Jenkins return " + response.code());//Create a dedicate Exception instead.
        } catch (IOException e) {
            throw new RuntimeException("Unable to configure Jenkins " + brickConfigurerData.getEntrypoint(), e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }

    private String getEntryPoint(BrickConfigurerData brickConfigurerData) {
        return brickConfigurerData.getEntrypoint() + SCRIPT_URL_SUFFIX;
    }

    private static void addAuthen(Request.Builder builder, String login, String password) {
        String crendential = String.format("%s:%s", login, password);
        String encodedBase64 = Base64.getEncoder().encodeToString(crendential.getBytes());
        builder.addHeader("Authorization", "Basic " + encodedBase64);
        if (LOGGER.isDebugEnabled()) {
            Request request = builder.build();
            LOGGER.debug("Request Jenkins url {} with following Header : Authorization : Basic ({}:{})", request.url().toString(), login, password);
        }
    }

}
