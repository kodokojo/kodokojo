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
package io.kodokojo.brick.nexus;

import com.squareup.okhttp.*;
import io.kodokojo.brick.BrickConfigurationException;
import io.kodokojo.brick.BrickConfigurer;
import io.kodokojo.brick.BrickConfigurerData;
import io.kodokojo.model.User;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class NexusConfigurer implements BrickConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NexusConfigurer.class);

    public static final String OLD_ADMIN_PASSWORD = "admin123";

    public static final String ADMIN_ACCOUNT_NAME = "admin";

    public static final String DEPLOYMENT_ACCOUNT_NAME = "deployment";

    private final OkHttpClient httpClient;

    public NexusConfigurer(OkHttpClient httpClient) {
        if (httpClient == null) {
            throw new IllegalArgumentException("httpClient must be defined.");
        }
        this.httpClient = httpClient;
    }

    @Override
    public BrickConfigurerData configure(BrickConfigurerData brickConfigurerData) throws BrickConfigurationException {
        String adminPassword = brickConfigurerData.getDefaultAdmin().getPassword();
        String xmlBody = getChangePasswordXmlBody(ADMIN_ACCOUNT_NAME, OLD_ADMIN_PASSWORD, adminPassword);

        if (changePassword(httpClient, brickConfigurerData.getEntrypoint(), xmlBody, ADMIN_ACCOUNT_NAME, OLD_ADMIN_PASSWORD)) {

            return brickConfigurerData;
        }
        throw new BrickConfigurationException("Unable to configure nexus " + brickConfigurerData.getEntrypoint());
    }

    @Override
    public BrickConfigurerData addUsers(BrickConfigurerData brickConfigurerData, List<User> users) throws BrickConfigurationException {
        if (brickConfigurerData == null) {
            throw new IllegalArgumentException("brickConfigurerData must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }
        OkHttpClient httpClient = this.httpClient;
        String adminPassword = brickConfigurerData.getDefaultAdmin().getPassword();
        for (User user : users) {
            String xmlBody = getCreatUserXmlBody(user);
            if (!createUser(httpClient, brickConfigurerData.getEntrypoint(), xmlBody, ADMIN_ACCOUNT_NAME, adminPassword)) {
                throw new BrickConfigurationException("Unable to add user '" + user.getUsername() + "' on nexus " + brickConfigurerData.getEntrypoint());
            }
        }
        return brickConfigurerData;
    }

    @Override
    public BrickConfigurerData removeUsers(BrickConfigurerData brickConfigurerData, List<User> users) {
        if (brickConfigurerData == null) {
            throw new IllegalArgumentException("brickConfigurerData must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }
        OkHttpClient httpClient = this.httpClient;
        String adminPassword = brickConfigurerData.getDefaultAdmin().getPassword();
        String url = brickConfigurerData.getEntrypoint() +"/service/local/users/";
        for (User user : users) {
            Request request = new Request.Builder().url(url+ user.getUsername())
                    .addHeader("Authorization", encodeBasicAuth(ADMIN_ACCOUNT_NAME, adminPassword))
                    .delete().build();

            Response response = null;
            try {
                response = httpClient.newCall(request).execute();
            } catch (IOException e) {
                LOGGER.error("Unable to delete userId {} on Nexus.", user.getUsername());
            } finally {
                if (response != null) {
                    IOUtils.closeQuietly(response.body());
                }
            }

        }
        return brickConfigurerData;
    }

    private boolean executeRequest(OkHttpClient httpClient, String url, String xmlBody, String login, String password) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/xml"), xmlBody);
        Request request = new Request.Builder().url(url)
                .addHeader("Authorization", encodeBasicAuth(login, password))
                .post(requestBody)
                .build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return response.code() >= 200 && response.code() < 300;
        } catch (IOException e) {
            LOGGER.error("Unable to complete request on Nexus url {}", url, e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return false;
    }

    private boolean changePassword(OkHttpClient httpClient, String baseUrl, String xmlBody, String login, String password) {
        return executeRequest(httpClient, baseUrl + "/service/local/users_changepw", xmlBody, login, password);
    }

    private boolean createUser(OkHttpClient httpClient, String baseUrl, String xmlBody, String login, String password) {
        return executeRequest(httpClient, baseUrl + "/service/local/users", xmlBody, login, password);
    }

    private String getChangePasswordXmlBody(String userId, String oldPassword, String newPassword) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<user-changepw>\n" +
                "<data>\n" +
                "<oldPassword>" + oldPassword + "</oldPassword>\n" +
                "<userId>" + userId + "</userId>\n" +
                "<newPassword>" + newPassword + "</newPassword>\n" +
                "</data>\n" +
                "</user-changepw>";
    }

    private String getCreatUserXmlBody(User user) {
        assert user != null : "User must be defined.";
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<user-request>\n" +
                "<data>\n" +
                "<userId>" + user.getUsername().toLowerCase() + "</userId>\n" +
                "<email>" + user.getEmail() + "</email>\n" +
                "<status>active</status>\n" +
                "<roles>\n" +
                "<role>nx-admin</role>\n" +
                "</roles>\n" +
                "<firstName>" + user.getFirstName() + "</firstName>\n" +
                "<lastName>" + user.getLastName() + "</lastName>\n" +
                "<password>" + user.getPassword() + "</password>\n" +
                "</data>\n" +
                "</user-request>";
    }

    private String encodeBasicAuth(String login, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", login, password).getBytes());
    }

}
