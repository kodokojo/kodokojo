package io.kodokojo.bdd;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.kodokojo.commons.utils.properties.PropertyResolver;
import io.kodokojo.commons.utils.properties.provider.OrderedMergedValueProvider;
import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.commons.utils.properties.provider.SystemEnvValueProvider;
import io.kodokojo.commons.utils.properties.provider.SystemPropertyValueProvider;
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
                try {
                    response.body().close();
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        }
        return false;
    }

    public String getMarathonUrl() {
        return "http://" + marathonConfig.marathonHost() + ":" + marathonConfig.marathonPort();
    }
}
