package io.kodokojo.project.jenkins;

import com.squareup.okhttp.*;
import io.kodokojo.project.launcher.ConfigurerData;
import io.kodokojo.project.launcher.ProjectConfigurer;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

public class JenkinsConfigurer implements ProjectConfigurer<ConfigurerData, Boolean> {

    private static final Properties VE_PROPERTIES = new Properties();

    private static final String SCRIPT_URL_SUFFIX = "/scriptText";

    private static final String INIT_JENKINS_GROOVY_VM = "init_jenkins.groovy.vm";

    private static final String USERS_KEY = "users";

    private static final String SCRIPT_KEY = "script";

    static {
        VE_PROPERTIES.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE_PROPERTIES.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        VE_PROPERTIES.setProperty("runtime.log.logsystem.class","org.apache.velocity.runtime.log.NullLogChute");
    }


    @Override
    public Boolean configure(ConfigurerData configurerData) {   

        String url = configurerData.getEntrypoint() + SCRIPT_URL_SUFFIX;

        OkHttpClient httpClient = new OkHttpClient();

        try {
            VelocityEngine ve = new VelocityEngine();
            ve.init(VE_PROPERTIES);


            Template template = ve.getTemplate(INIT_JENKINS_GROOVY_VM);

            VelocityContext context = new VelocityContext();
            context.put(USERS_KEY, configurerData.getUsers());

            StringWriter sw = new StringWriter();
            template.merge(context, sw);
            String script = sw.toString();

            RequestBody body = new FormEncodingBuilder().add(SCRIPT_KEY, script).build();

            Request request = new Request.Builder().url(url).post(body).build();
            Response response = httpClient.newCall(request).execute();
            return response.code() == 200 ? Boolean.TRUE : Boolean.FALSE;
        } catch (IOException e) {
            throw new RuntimeException("Unable to configure Jenkins " + configurerData.getEntrypoint(), e);
        }
    }

}
