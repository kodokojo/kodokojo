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
package io.kodokojo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.kodokojo.config.module.*;
import io.kodokojo.config.module.endpoint.BrickEndpointModule;
import io.kodokojo.config.module.endpoint.ProjectEndpointModule;
import io.kodokojo.config.module.endpoint.UserEndpointModule;
import io.kodokojo.endpoint.HttpEndpoint;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    //  WebSocket is built by Spark but we are not able to get the instance :/ .
    //  See : https://github.com/perwendel/spark/pull/383
    public static Injector INJECTOR;

    public static void main(String[] args) {

        LOGGER.info("Starting Kodo Kojo.");

        INJECTOR = Guice.createInjector(new PropertyModule(args),
                new SecurityModule(),
                new RedisModule(),
                new ServiceModule(),
                new ActorModule(),
                new AwsModule(),
                new MarathonModule(),
                new UserEndpointModule(),
                new BrickEndpointModule(),
                new ProjectEndpointModule(),
                new RestEndpointModule()
        );

        ApplicationLifeCycleManager applicationLifeCycleManager = INJECTOR.getInstance(ApplicationLifeCycleManager.class);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                LOGGER.info("Stopping services.");
                applicationLifeCycleManager.stop();
                LOGGER.info("All services stopped.");
            }
        });

        HttpEndpoint httpEndpoint = INJECTOR.getInstance(HttpEndpoint.class);
        httpEndpoint.start();

        LOGGER.info("Kodo Kojo started.");

    }

}
