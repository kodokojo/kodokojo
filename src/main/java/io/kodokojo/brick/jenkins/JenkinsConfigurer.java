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
package io.kodokojo.brick.jenkins;


import com.squareup.okhttp.*;
import io.kodokojo.brick.BrickConfigurer;
import io.kodokojo.brick.BrickConfigurerData;
import io.kodokojo.model.User;
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
import java.util.Base64;
import java.util.List;
import java.util.Properties;

public class JenkinsConfigurer implements BrickConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsConfigurer.class);

    private static final Properties VE_PROPERTIES = new Properties();

    private static final String SCRIPT_URL_SUFFIX = "/scriptText";

    private static final String INIT_JENKINS_GROOVY_VM = "init_jenkins.groovy.vm";

    private static final String ADD_USER_JENKINS_GROOVY_VM = "add_user_jenkins.groovy.vm";

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
    public BrickConfigurerData configure(BrickConfigurerData brickConfigurerData) {
        VelocityContext context = new VelocityContext();
        context.put(USERS_KEY, brickConfigurerData.getUsers());
        String templatePath = INIT_JENKINS_GROOVY_VM;

        return executeGroovyScript(brickConfigurerData, context, templatePath);
    }

    @Override
    public BrickConfigurerData addUsers(BrickConfigurerData brickConfigurerData, List<User> users) {
        VelocityContext context = new VelocityContext();
        context.put(USERS_KEY, brickConfigurerData.getUsers());
        String templatePath = ADD_USER_JENKINS_GROOVY_VM;
        return executeGroovyScript(brickConfigurerData, context, templatePath);
    }

    @Override
    public BrickConfigurerData removeUsers(BrickConfigurerData brickConfigurerData, List<User> users) {
        VelocityContext context = new VelocityContext();
        context.put(USERS_KEY, brickConfigurerData.getUsers());
        String templatePath = DELETE_USER_JENKINS_GROOVY_VM;
        return executeGroovyScript(brickConfigurerData, context, templatePath);
    }

    private BrickConfigurerData executeGroovyScript(BrickConfigurerData brickConfigurerData, VelocityContext context, String templatePath) {
        String url = brickConfigurerData.getEntrypoint() + SCRIPT_URL_SUFFIX;

        Response response = null;
        try {
            VelocityEngine ve = new VelocityEngine();
            ve.init(VE_PROPERTIES);


            Template template = ve.getTemplate(templatePath);


            StringWriter sw = new StringWriter();
            template.merge(context, sw);
            String script = sw.toString();

            RequestBody body = new FormEncodingBuilder().add(SCRIPT_KEY, script).build();

            Request.Builder builder = new Request.Builder().url(url).post(body);
            User admin = brickConfigurerData.getDefaultAdmin();
            
            String crendential = String.format("%s:%s", admin.getUsername(), admin.getPassword());
            builder.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(crendential.getBytes()));
            Request request = builder.build();
            response = httpClient.newCall(request).execute();
            //if (response.code() >= 200 && response.code() < 300) {
                return brickConfigurerData;
            //}
            //hrow new RuntimeException("Unable to configure Jenkins " + brickConfigurerData.getEntrypoint() + ". Jenkins return " + response.code());//Create a dedicate Exception instead.
        } catch (IOException e) {
            throw new RuntimeException("Unable to configure Jenkins " + brickConfigurerData.getEntrypoint(), e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }

}
