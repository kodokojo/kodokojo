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
package io.kodokojo.service.marathon;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import io.kodokojo.config.MarathonConfig;
import io.kodokojo.model.PortDefinition;
import io.kodokojo.model.Service;
import io.kodokojo.test.utils.DataBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Ignore
@RunWith(DataProviderRunner.class)
public class MarathonServiceLocatorTest implements DataBuilder {

    @DataProvider
    public static Object[][] serviceData() {
        return new Object[][]{
                {JENKINS, "xebia-jenkins-8080", "10.10.62.239", 8080, 17793, PortDefinition.Type.HTTPS},
                {NEXUS, "xebia-nexus-8081", "10.10.78.34", 8081, 11259, PortDefinition.Type.HTTPS}
        };
    }

    private static final String NEXUS = "nexus";

    private static final String JENKINS = "jenkins";

    private static final String GITLAB = "gitlab";

    private static final String XEBIA = "xebia";

    private MarathonServiceLocator marathonServiceLocator;

    @Test
    @UseDataProvider("serviceData")
    public void locate_service(String brickName, String serviceName, String host, int containerPort, int hostPort, PortDefinition.Type portType) {
        // given

        // when
        Set<Service> services = marathonServiceLocator.getService(brickName, XEBIA);

        // then
        assertThat(services.size()).isEqualTo(1);
        Service service = services.iterator().next();
        assertThat(service.getName()).isEqualTo(serviceName);
        assertThat(service.getHost()).isEqualTo(host);
        PortDefinition portDefinition = service.getPortDefinition();
        assertThat(portDefinition.getContainerPort()).isEqualTo(containerPort);
        assertThat(portDefinition.getHostPort()).isEqualTo(hostPort);
        assertThat(portDefinition.getType()).isEqualTo(portType);

    }

    @Test
    public void locate_gitlab() {
        // given

        // when
        Set<Service> services = marathonServiceLocator.getService(GITLAB, XEBIA);

        // then
        assertThat(services.size()).isEqualTo(2);
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            assertThat(service.getHost()).isEqualTo("10.10.78.34");
            if ("xebia-gitlab-80".equals(service.getName())) {
                PortDefinition portDefinition = service.getPortDefinition();
                assertThat(portDefinition.getContainerPort()).isEqualTo(80);
                assertThat(portDefinition.getHostPort()).isEqualTo(13171);
                assertThat(portDefinition.getType()).isEqualTo(PortDefinition.Type.HTTPS);
            } else if ("xebia-gitlab-22".equals(service.getName())) {
                PortDefinition portDefinition = service.getPortDefinition();
                assertThat(portDefinition.getContainerPort()).isEqualTo(22);
                assertThat(portDefinition.getHostPort()).isEqualTo(13172);
                assertThat(portDefinition.getType()).isEqualTo(PortDefinition.Type.SSH);
            } else {
                fail("Unknown service name " + service.getName());
            }
        }

    }

    @Before
    public void setup() {
        marathonServiceLocator = new MarathonServiceLocator(aMarathonConfig()) {
            @Override
            protected MarathonServiceLocatorRestApi provideMarathonRestApi(MarathonConfig marathonConfig) {
                return null;
            }
        };
    }
/*
    private class TestMarathonRestApi implements MarathonServiceLocatorRestApi {

        @Override
        public Call<JsonObject> getAllApplications() {
            Call mock = Mockito.mock(Call.class);
            Response response = Mockito.mock(Response.class);
            try {
                Mockito.when(mock.execute()).thenReturn(response);
                Mockito.when(response.body()).thenReturn(getJsonObjectFromFile("allApps.json"));
            } catch (IOException e) {
                fail(e.getMessage());
            }
            return mock;
        }

        @Override
        public Call<JsonObject> getApplicationConfiguration(@Path("appId") String appId) {
            String fileName = "allApps.json";
            switch (appId) {
                case "/" + XEBIA + "/" + NEXUS:
                    fileName = "nexus.json";
                    break;
                case "/" + XEBIA + "/" + JENKINS:
                    fileName = "jenkins.json";
                    break;
                case "/" + XEBIA + "/" + GITLAB:
                    fileName = "gitlab.json";
                    break;
                default:
                    break;
            }
            Call mock = Mockito.mock(Call.class);
            Response response = Mockito.mock(Response.class);
            try {
                Mockito.when(mock.execute()).thenReturn(response);
                Mockito.when(response.body()).thenReturn(getJsonObjectFromFile(fileName));
            } catch (IOException e) {
                fail(e.getMessage());
            }
            return mock;
        }

        private JsonObject getJsonObjectFromFile(String fileName) {
            try {
                String content = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("marathon/" + fileName));
                JsonParser parser = new JsonParser();
                return (JsonObject) parser.parse(content);
            } catch (IOException e) {
                Assertions.fail(e.getMessage());
            }
            return null;
        }

    }
    */


}