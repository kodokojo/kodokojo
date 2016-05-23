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
package io.kodokojo.endpoint;


import io.kodokojo.model.User;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.service.authentification.SimpleCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import javax.inject.Inject;
import java.util.Set;

import static spark.Spark.*;

public class HttpEndpoint extends AbstractSparkEndpoint implements ApplicationLifeCycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEndpoint.class);

    private final int port;

    private final Set<SparkEndpoint> sparkEndpoints;

    @Inject
    public HttpEndpoint(int port, UserAuthenticator<SimpleCredential> userAuthenticator, Set<SparkEndpoint> sparkEndpoints) {
        super(userAuthenticator);
        if (sparkEndpoints == null) {
            throw new IllegalArgumentException("sparkEndpoints must be defined.");
        }
        this.port = port;
        this.sparkEndpoints = sparkEndpoints;
    }

    @Override
    public void start() {
        configure();
    }

    @Override
    public void configure() {

        Spark.port(port);

        webSocket(BASE_API + "/event", WebSocketEntryPoint.class);

        staticFileLocation("webapp");

        before((request, response) -> {
            boolean authenticationRequired = true;
            // White list of url which not require to have an identifier.
            if (requestMatch("POST", BASE_API + "/user", request) ||
                    requestMatch("GET", BASE_API, request) ||
                    requestMatch("GET", BASE_API + "/event", request) ||
                    requestMatch("GET", BASE_API + "/doc(/)?.*", request) ||
                    requestMatch("POST", BASE_API + "/user/[^/]*", request)
                    ) {
                authenticationRequired = false;
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Authentication is {}require for request {} {}.", authenticationRequired ? "" : "NOT ", request.requestMethod(), request.pathInfo());
            }
            if (authenticationRequired) {
                BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
                basicAuthenticator.handle(request, response);
                if (basicAuthenticator.isProvideCredentials()) {
                    User user = userAuthenticator.authenticate(new SimpleCredential(basicAuthenticator.getUsername(), basicAuthenticator.getPassword()));
                    if (user == null) {
                        authorizationRequiered(response);

                    }
                } else {
                    authorizationRequiered(response);
                }
            }
        });

        sparkEndpoints.forEach(SparkEndpoint::configure);

        get(BASE_API, JSON_CONTENT_TYPE, (request, response) -> {
            response.type(JSON_CONTENT_TYPE);
            return "{\"version\":\"1.0.0\"}";
        });

        Spark.awaitInitialization();
        LOGGER.info("Spark server started on port {}.", port);
    }

    public int getPort() {
        return port;
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping HttpEndpoint.");
        Spark.stop();
    }

    private static void authorizationRequiered(Response response) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Current request required an authentication which not currently provide.");
        }
        response.header("WWW-Authenticate", "Basic realm=\"Kodokojo\"");
        response.status(401);
        halt(401);
    }

    private static boolean requestMatch(String methodName, String regexpPath, Request request) {
        boolean matchMethod = methodName.equals(request.requestMethod());
        boolean pathMatch = request.pathInfo().matches(regexpPath);
        return matchMethod && pathMatch;
    }

}
