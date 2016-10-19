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
package io.kodokojo.bdd;

import io.kodokojo.config.properties.PropertyResolver;
import io.kodokojo.config.properties.provider.OrderedMergedValueProvider;
import io.kodokojo.config.properties.provider.PropertyValueProvider;
import io.kodokojo.config.properties.provider.SystemEnvValueProvider;
import io.kodokojo.config.properties.provider.SystemPropertyValueProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.fail;

public class MarathonIsPresent implements MethodRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonIsPresent.class);

    private final MarathonConfig marathonConfig;

    private final OkHttpClient httpClient;

    public MarathonIsPresent() {
        LinkedList<PropertyValueProvider> valueproviders = new LinkedList<>();
        valueproviders.add(new SystemEnvValueProvider());
        valueproviders.add(new SystemPropertyValueProvider());
        PropertyValueProvider valueProvider = new OrderedMergedValueProvider(valueproviders);
        PropertyResolver resolver = new PropertyResolver(valueProvider);
        marathonConfig = resolver.createProxy(MarathonConfig.class);
        httpClient = new OkHttpClient();
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        MarathonIsRequire marathonIsRequire = method.getAnnotation(MarathonIsRequire.class);
        if (marathonIsRequire == null) {
            return base;
        }
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Assume.assumeTrue("Marathon must be present", marathonIsRunning());
                base.evaluate();
            }
        };
    }

    public String getMarathonHost() {
        return marathonConfig.marathonHost();
    }

    public int getMarathonPort() {
        return marathonConfig.marathonPort();
    }

    public boolean marathonIsRunning() {
        Request request = new Request.Builder().url(getMarathonUrl() + "/ping").get().build();
        Response response = null;
        try {
             response = httpClient.newCall(request).execute();
            return response.code() == 200;
        } catch (IOException e) {
            LOGGER.error("unable to done request {} due to following error", e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return false;
    }

    public String getMarathonUrl() {
        return "http://" + marathonConfig.marathonHost() + ":" + marathonConfig.marathonPort();
    }
}
