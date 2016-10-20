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
package io.kodokojo.brick.nexus;

import io.kodokojo.brick.BrickConfigurationException;
import io.kodokojo.brick.BrickConfigurer;
import io.kodokojo.brick.BrickConfigurerData;
import io.kodokojo.brick.BrickConfigurerHelper;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.UpdateData;
import io.kodokojo.model.User;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class NexusConfigurer implements BrickConfigurer, BrickConfigurerHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NexusConfigurer.class);

    public static final String OLD_ADMIN_PASSWORD = "admin123";

    public static final String ADMIN_ACCOUNT_NAME = "admin";

    public static final String DEPLOYMENT_ACCOUNT_NAME = "deployment";

    public static final String APPLICATION_XML = "application/xml";

    private final OkHttpClient httpClient;

    public NexusConfigurer(OkHttpClient httpClient) {
        if (httpClient == null) {
            throw new IllegalArgumentException("httpClient must be defined.");
        }
        this.httpClient = httpClient;
    }

    @Override
    public BrickConfigurerData configure(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData) throws BrickConfigurationException {
        String adminPassword = getAdminPassword(projectConfiguration);
        String xmlBody = getChangePasswordXmlBody(ADMIN_ACCOUNT_NAME, OLD_ADMIN_PASSWORD, adminPassword);

        if (changePassword(httpClient, brickConfigurerData.getEntrypoint(), xmlBody, ADMIN_ACCOUNT_NAME, OLD_ADMIN_PASSWORD)) {

            return brickConfigurerData;
        }
        throw new BrickConfigurationException("Unable to configure nexus " + brickConfigurerData.getEntrypoint());
    }

    @Override
    public BrickConfigurerData addUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData, List<User> users) throws BrickConfigurationException {
        requireNonNull(brickConfigurerData, "brickConfigurerData must be defined.");
        requireNonNull(users, "users must be defined.");

        String adminPassword = getAdminPassword(projectConfiguration);
        for (User user : users) {
            String xmlBody = getCreatUserXmlBody(user);
            if (!createUser(httpClient, brickConfigurerData.getEntrypoint(), xmlBody, ADMIN_ACCOUNT_NAME, adminPassword)) {
                throw new BrickConfigurationException("Unable to add user '" + user.getUsername() + "' on nexus " + brickConfigurerData.getEntrypoint());
            }
        }
        return brickConfigurerData;
    }

    private String getAdminPassword(ProjectConfiguration projectConfiguration) {
        return projectConfiguration.getUserService().getPassword();
    }

    @Override
    public BrickConfigurerData updateUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData, List<UpdateData<User>> users) {
        requireNonNull(brickConfigurerData, "brickConfigurerData must be defined.");
        requireNonNull(users, "users must be defined.");

        String adminPassword = getAdminPassword(projectConfiguration);

        for (UpdateData<User> entry : users) {
            User user = entry.getNewData();
            String xmlBody = getUpdateUserAccountXmlBody(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName());
            updateAccount(httpClient, brickConfigurerData.getEntrypoint(), xmlBody, ADMIN_ACCOUNT_NAME, adminPassword, user.getUsername());
            xmlBody = getChangePasswordXmlBody(user.getUsername(), entry.getOldData().getPassword() , user.getPassword());
            changePassword(httpClient, brickConfigurerData.getEntrypoint(), xmlBody, ADMIN_ACCOUNT_NAME, adminPassword);
        }
        return brickConfigurerData;
    }

    @Override
    public BrickConfigurerData removeUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData, List<User> users) {
        requireNonNull(brickConfigurerData, "brickConfigurerData must be defined.");
        requireNonNull(users, "users must be defined.");

        String adminPassword = getAdminPassword(projectConfiguration);
        String url = brickConfigurerData.getEntrypoint() + "/service/local/users/";
        for (User user : users) {
            Request.Builder builder = new Request.Builder().url(url + user.getUsername())
                    .delete();
            addBasicAuthentificationHeader(builder, ADMIN_ACCOUNT_NAME, adminPassword);
            Request request = builder.build();

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
        return executeRequest(httpClient, url, xmlBody, login, password, false);
    }

    private boolean executeRequest(OkHttpClient httpClient, String url, String xmlBody, String login, String password, boolean put) {
        RequestBody requestBody = RequestBody.create(MediaType.parse(APPLICATION_XML), xmlBody);
        Request.Builder builder = new Request.Builder()
                .url(url);
        if (put) {
            builder.put(requestBody);
        } else {
            builder.post(requestBody);
        }
        addBasicAuthentificationHeader(builder, login, password);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Request url {} with login '{}' and password {}blank.", url, login, (StringUtils.isNotBlank(password) ? "NOT " : ""));
        }
        Request request = builder
                .build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Request on URL {} return code {}.", url, response.code());
            }
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

    private boolean updateAccount(OkHttpClient httpClient, String baseUrl, String xmlBody, String login, String password, String userId) {
        return executeRequest(httpClient, baseUrl + "/service/local/user_account/" + userId, xmlBody, login, password, true);
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

    private String getUpdateUserAccountXmlBody(String userId, String email, String firstName, String lastName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<user-account-request>\n" +
                "  <data>\n" +
                "    <email>" + email + "</email>\n" +
                "    <firstName>" + firstName + "</firstName>\n" +
                "    <userId>" + userId + "</userId>\n" +
                "    <lastName>" + lastName + "</lastName>\n" +
                "  </data>\n" +
                "</user-account-request>";
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


}
