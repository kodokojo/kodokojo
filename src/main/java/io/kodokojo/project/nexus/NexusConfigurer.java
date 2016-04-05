package io.kodokojo.project.nexus;

import com.squareup.okhttp.*;
import io.kodokojo.model.User;
import io.kodokojo.project.starter.BrickConfigurer;
import io.kodokojo.project.starter.ConfigurerData;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class NexusConfigurer implements BrickConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NexusConfigurer.class);

    public static final String OLD_ADMIN_PASSWORD = "admin123";

    public static final String ADMIN_ACCOUNT_NAME = "admin";

    public static final String DEPLOYMENT_ACCOUNT_NAME = "deployment";

    @Override
    public ConfigurerData configure(ConfigurerData configurerData) {

        OkHttpClient httpClient = provideHttpClient();

        String adminPassword = configurerData.getAdminUser().getPassword();
        String xmlBody = getChangePasswordXmlBody(ADMIN_ACCOUNT_NAME, OLD_ADMIN_PASSWORD, adminPassword);

        changePassword(httpClient, configurerData.getEntrypoint(), xmlBody, ADMIN_ACCOUNT_NAME, OLD_ADMIN_PASSWORD);

        return configurerData;
    }


    @Override
    public ConfigurerData addUsers(ConfigurerData configurerData, List<User> users) {
        if (configurerData == null) {
            throw new IllegalArgumentException("configurerData must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }
        OkHttpClient httpClient = provideHttpClient();
        String adminPassword = configurerData.getAdminUser().getPassword();
        for (User user : users) {
            String xmlBody = getCreatUserXmlBody(user);
            createUser(httpClient, configurerData.getEntrypoint(), xmlBody, ADMIN_ACCOUNT_NAME, adminPassword);
        }
        return configurerData;
    }

    protected OkHttpClient provideHttpClient() {
        return new OkHttpClient();
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
            return response.code() == 202;
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

    public static void main(String[] args) {
        NexusConfigurer nexusConfigurer = new NexusConfigurer();
        User admin = new User("1234", "Jean-Pascal THIERY", "jpthiery", "jpthiery@kodokojo.io", "jpthiery", "an SSH public key");
        List<User> users = Collections.singletonList(admin);
        ConfigurerData configurerData = new ConfigurerData("Acme", "http://52.16.137.170:49598", "kodokojo.io", admin, users);

        configurerData = nexusConfigurer.configure(configurerData);
        nexusConfigurer.addUsers(configurerData, users);
    }

}
