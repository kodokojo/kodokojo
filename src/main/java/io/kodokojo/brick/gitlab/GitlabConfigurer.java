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
package io.kodokojo.brick.gitlab;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.kodokojo.brick.*;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.UpdateData;
import io.kodokojo.model.User;
import javaslang.control.Try;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class GitlabConfigurer implements BrickConfigurer, BrickConfigurerHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabConfigurer.class);

    public static final String GITLAB_ADMIN_TOKEN_KEY = "GITLAB_ADMIN_API_TOKEN";

    public static final String GITLAB_FORCE_ENTRYPOINT_KEY = "GITLAB_FORCE_USE_DEFAULT_ENTRYPOINT";

    private static final Pattern FORM_TOKEN_PATTERN = Pattern.compile(".*<input type=\"hidden\" name=\"authenticity_token\" value=\"([^\"]*)\" />.*");

    private static final Pattern URL_RESET_TOKEN_PATTERN = Pattern.compile("reset_password_token=(.*)$");

    private static final Pattern PRIVATE_TOKEN_PATTERN = Pattern.compile(".*<input type=\"text\" name=\"token\" id=\"token\" value=\"([^\"]*)\" class=\"form-control\" />.*");

    private static final String SIGNIN_URL = "/users/sign_in";

    private static final String PASSWORD_URL = "/users/password";

    private static final String ACCOUNT_URL = "/profile/account";

    private static final String ROOT_LOGIN = "root";

    private static final String GITLAB_CHANGE_FAIL_MESSAGE = "After a successful password update you will be redirected to login screen.";

    private static final String KODO_KOJO_SSH_KEY = "Kodo Kojo SSH Key";
    public static final String PRIVATE_TOKEN = "PRIVATE-TOKEN";
    public static final String API_V3_USERS = "/api/v3/users/";

    private final BrickUrlFactory brickUrlFactory;

    @Inject
    public GitlabConfigurer(BrickUrlFactory brickUrlFactory) {
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        this.brickUrlFactory = brickUrlFactory;
    }

    @Override
    public BrickConfigurerData configure(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData) throws BrickConfigurationException {
        String gitlabUrl = getGitlabEntryPoint(brickConfigurerData);
        OkHttpClient httpClient = provideDefaultOkHttpClient();

        try {
            Thread.sleep(120000);    // Waiting for Gitlab fully start. We don't have any strong way to defined if Gitlab is ready or not.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long end = System.currentTimeMillis() + 180000;
        String changePasswordUrl = "";
        String resetToken = "";

        do {
            changePasswordUrl = getChangePasswordUrl(httpClient, gitlabUrl);
            resetToken = getResetToken(changePasswordUrl);
            if (StringUtils.isBlank(resetToken)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } while (StringUtils.isBlank(resetToken) && System.currentTimeMillis() < end);


        if (StringUtils.isBlank(resetToken)) {
            throw new BrickConfigurationException("Unable to get the reset token for gitlab  for project " + brickConfigurerData.getProjectName() + " on url " + gitlabUrl);
        }
        String newPassword = projectConfiguration.getUserService().getPassword();

        String authenticityToken = getAuthenticityToken(httpClient, changePasswordUrl, FORM_TOKEN_PATTERN);
        if (changeDefaultPassword(httpClient, gitlabUrl + PASSWORD_URL, resetToken, authenticityToken, newPassword)) {
            if (signIn(httpClient, gitlabUrl, ROOT_LOGIN, newPassword)) {
                String url = gitlabUrl + ACCOUNT_URL;
                Request request = new Request.Builder().get().url(url).build();
                Response response = null;
                try {
                    response = httpClient.newCall(request).execute();
                    authenticityToken = getAuthenticityToken(response.body().string(), PRIVATE_TOKEN_PATTERN);
                    if (StringUtils.isBlank(authenticityToken)) {
                        String message = "Unable to get private token of root account for gitlab  for project " + brickConfigurerData.getProjectName() + " on url " + gitlabUrl;
                        LOGGER.error(message);
                        throw new BrickConfigurationException(message);
                    }
                    brickConfigurerData.addInContext(GITLAB_ADMIN_TOKEN_KEY, authenticityToken);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("return {}={}", GITLAB_ADMIN_TOKEN_KEY, authenticityToken);
                        LOGGER.debug("return BrickConfigurerData: {}", brickConfigurerData);
                    }
                    return brickConfigurerData;
                } catch (IOException e) {
                    LOGGER.error("Unable to retrieve account page", e);
                } finally {
                    if (response != null) {
                        IOUtils.closeQuietly(response.body());
                    }
                }
            } else {
                LOGGER.error("Unable to log on Gitlab with new password");
                String message = "Unable to log on Gitlab with new password for project " + brickConfigurerData.getProjectName() + " on url " + gitlabUrl;
                LOGGER.error(message);
                throw new BrickConfigurationException(message);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.warn("Unable to change admin password on Gitabl for url {}.", gitlabUrl);
        }


        return brickConfigurerData;
    }


    @Override
    public BrickConfigurerData addUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData, List<User> users) throws BrickConfigurationException {
        if (brickConfigurerData == null) {
            throw new IllegalArgumentException("brickConfigurerData must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }

        GitlabRest gitlabRest = provideGitlabRest(brickConfigurerData);

        String gitlabEntryPoint = getGitlabEntryPoint(brickConfigurerData);

        OkHttpClient httpClient = provideDefaultOkHttpClient();

        String privateToken = (String) brickConfigurerData.getContext().get(GITLAB_ADMIN_TOKEN_KEY);

        for (User user : users) {
            Try<Integer> userId = createUser(gitlabRest, httpClient, gitlabEntryPoint, privateToken, user);
            if (userId.isSuccess()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("User '{}' successfully added to Gitlab {}.", user.getUsername(), gitlabEntryPoint);
                }
            } else {
                String message = "Unable to create user '" + user.getUsername() + "' for project " + brickConfigurerData.getProjectName() + " on url " + gitlabEntryPoint;
                LOGGER.error(message);
            }
        }

        return brickConfigurerData;
    }


    @Override
    public BrickConfigurerData updateUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData, List<UpdateData<User>> users) {
        String privateToken = (String) brickConfigurerData.getContext().get(GITLAB_ADMIN_TOKEN_KEY);

        GitlabRest gitlabRest = provideGitlabRest(brickConfigurerData);

        users.stream().map(UpdateData::getNewData)
                .forEach(u -> {
                    String userId = lookupUserId(gitlabRest, privateToken, u);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Trying to update user '{}' on  Gitlab.", u);
                    }
                    retrofit2.Call<retrofit2.Response> updateCall = gitlabRest.update(privateToken, userId, u.getUsername(), u.getName(), u.getPassword(), u.getEmail());
                    try {
                        retrofit2.Response<retrofit2.Response> updateResponse = updateCall.execute();
                        if (updateResponse.isSuccessful()) {
                            retrofit2.Call<JsonArray> call = gitlabRest.listSshKeys(privateToken, userId);
                            JsonArray listSshKeys = call.execute().body();

                            for (JsonElement keyEl : listSshKeys) {
                                JsonObject keyJson = (JsonObject) keyEl;
                                if (KODO_KOJO_SSH_KEY.equals(keyJson.getAsJsonPrimitive("title").getAsString())) {
                                    int keyId = keyJson.getAsJsonPrimitive("id").getAsInt();
                                    retrofit2.Call<retrofit2.Response> deleteCall = gitlabRest.deleteSshKey(privateToken, userId, "" + keyId);
                                    deleteCall.execute();
                                    addSshKey(provideDefaultOkHttpClient(), getGitlabEntryPoint(brickConfigurerData), privateToken, Integer.parseInt(userId), u);
                                }
                            }
                        }

                    } catch (IOException e) {
                        LOGGER.error("Unable to execute a request on Gitlab {}", getGitlabEntryPoint(brickConfigurerData), e);
                    }
                });

        return brickConfigurerData;
    }


    @Override
    public BrickConfigurerData removeUsers(ProjectConfiguration projectConfiguration, BrickConfigurerData brickConfigurerData, List<User> users) {
        if (brickConfigurerData == null) {
            throw new IllegalArgumentException("brickConfigurerData must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }

        GitlabRest gitlabRest = provideGitlabRest(brickConfigurerData);

        String privateToken = (String) brickConfigurerData.getContext().get(GITLAB_ADMIN_TOKEN_KEY);
        for (User user : users) {
            String id = lookupUserId(gitlabRest, privateToken, user);
            gitlabRest.deleteUser(privateToken, id);

        }
        return brickConfigurerData;
    }

    protected GitlabRest provideGitlabRest(BrickConfigurerData brickConfigurerData) {
        String gitlabEntryPoint = getGitlabEntryPoint(brickConfigurerData);
        OkHttpClient httpClient = provideDefaultOkHttpClient();
        Retrofit adapter = new Retrofit.Builder().baseUrl(gitlabEntryPoint).client(httpClient).build();
        return adapter.create(GitlabRest.class);
    }

    private String lookupUserId(GitlabRest gitlabRest, String privateToken, User user) {
        retrofit2.Call<JsonArray> searchCall = gitlabRest.searchByUsername(privateToken, user.getUsername());
        JsonArray results = null;
        try {
            results = searchCall.execute().body();
            Iterator<JsonElement> it = results.iterator();
            String id = null;
            while (id == null && it.hasNext()) {
                JsonObject json = (JsonObject) it.next();
                if (json.has("id")) {
                    id = json.getAsJsonPrimitive("id").getAsString();
                }
            }
            return id;
        } catch (IOException e) {
            LOGGER.error("Unable to lookup user '{}.", user.getUsername(), e);
        }
        return null;

    }

    private String getGitlabEntryPoint(BrickConfigurerData brickConfigurerData) {
        Boolean forceDefault = (Boolean) brickConfigurerData.getContext().get(GITLAB_FORCE_ENTRYPOINT_KEY);
        if (forceDefault != null && forceDefault) {
            return brickConfigurerData.getEntrypoint();
        }
        return "https://" + brickUrlFactory.forgeUrl(brickConfigurerData.getProjectName(), brickConfigurerData.getStackName(), "scm", "gitlab");
    }

    private static String getChangePasswordUrl(OkHttpClient httpClient, String gitlabUrl) {
        Request request = new Request.Builder().get().url(gitlabUrl + SIGNIN_URL).build();
        Response response = null;
        String res = null;
        try {
            response = httpClient.newCall(request).execute();
            //LOGGER.debug(response.toString());
            res = response.request().url().toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return res;
    }

    private static String getResetToken(String changePasswordUrl) {
        //LOGGER.debug("Extract token from url {}.", changePasswordUrl);
        Matcher matcher = URL_RESET_TOKEN_PATTERN.matcher(changePasswordUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static boolean changeDefaultPassword(OkHttpClient httpClient, String url, String resetToken, String token, String newPassword) {
        RequestBody formBody = new FormBody.Builder()
                .addEncoded("utf8", "%E2%9C%93")
                .add("authenticity_token", token)
                .add("_method", "put")
                .add("user[reset_password_token]", resetToken)
                .add("user[password]", newPassword)
                .add("user[password_confirmation]", newPassword)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return response.code() == 200 && response.body().string().contains("Your password has been changed successfully.");
        } catch (IOException e) {
            LOGGER.error("Unable to create admin account&", e);
            return false;
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }

    }

    public static boolean signIn(OkHttpClient httpClient, String gitlabUrl, String login, String password) {
        String token = getAuthenticityToken(httpClient, gitlabUrl + SIGNIN_URL, FORM_TOKEN_PATTERN);
        RequestBody formBody = new FormBody.Builder()
                .addEncoded("utf8", "%E2%9C%93")
                .add("authenticity_token", token)
                .add("user[login]", login)
                .add("user[password]", password)
                .add("user[remember_me]", "0")
                .build();

        Request request = new Request.Builder()
                .url(gitlabUrl + SIGNIN_URL)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody).build();

        Call call = httpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            //LOGGER.debug("Signin on {} with login {}: {}", gitlabUrl + SIGNIN_URL, login, response.toString());
            String body = response.body().string();
            return response.isSuccessful() && !body.contains("Invalid login or password.");
        } catch (IOException e) {
            LOGGER.error("Unable to change default password for Gitlab", e);
            return false;
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }


    private static String getAuthenticityToken(OkHttpClient httpClient, String url, Pattern pattern) {
        Request request = new Request.Builder().url(url).get().build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            String bodyReponse = response.body().string();
            return getAuthenticityToken(bodyReponse, pattern);
        } catch (IOException e) {
            LOGGER.error("Unable to request " + url, e);
        } finally {
            if (response != null && response.body() != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        return null;
    }

    private Try<Integer> createUser(GitlabRest gitlabRest, OkHttpClient httpClient, String gitlabUrl, String privateToken, User user) {
        String url = gitlabUrl + "/api/v3/users";

        RequestBody formBody = new FormBody.Builder()
                .add("username", user.getUsername())
                .add("password", user.getPassword())
                .add("name", user.getName())
                .add("email", user.getEmail())
                .add("admin", "true")
                .add("confirm", "true")
                .build();
        Request request = new Request.Builder().url(url)
                .post(formBody)
                .header(PRIVATE_TOKEN, privateToken)
                .build();

        int id = -1;
        Response response = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to add User '{}' to Gitlab: {}", user.getUsername(), gitlabUrl);
            }

            response = httpClient.newCall(request).execute();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Create user on Gitlab on url {}; response {} .", gitlabUrl, response.code());
            }

            if (response.code() != 201) {
                LOGGER.debug("Not able to create user ? {}", response.body().string());
            }

            JsonParser parser = new JsonParser();
            JsonObject jsonObject = (JsonObject) parser.parse(response.body().string());


            id = jsonObject.getAsJsonPrimitive("id").getAsInt();

        } catch (IOException e) {
            LOGGER.error("Unable to create user on Gitlab {}.", gitlabUrl, e);
            return Try.failure(e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }


        addSshKey(httpClient, gitlabUrl, privateToken, id, user);

        return Try.success(id);
    }

    private void addSshKey(OkHttpClient httpClient, String gitlabUrl, String privateToken, int userId, User user) {

        RequestBody formBody;
        Request request;
        formBody = new FormBody.Builder()
                .add("title", KODO_KOJO_SSH_KEY)
                .add("key", user.getSshPublicKey())
                .build();
        request = new Request.Builder().post(formBody)
                .url(gitlabUrl + API_V3_USERS + userId + "/keys")
                .addHeader(PRIVATE_TOKEN, privateToken)
                .build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            boolean sshKeyAdded = response.code() >= 200 && response.code() < 300;
            if (response.code() == 500) {
                IOUtils.closeQuietly(response.body());
                request = new Request.Builder().get()
                        .url(gitlabUrl + API_V3_USERS + userId + "/keys")
                        .addHeader(PRIVATE_TOKEN, privateToken)
                        .build();
                response = httpClient.newCall(request).execute();
                String body = response.body().string();

                JsonParser parser = new JsonParser();
                JsonElement json = parser.parse(body);
                if (response.code() == 200) {
                    JsonArray keys = (JsonArray) json;
                    sshKeyAdded = keys.size() > 0;
                    long timeout = System.currentTimeMillis() + 240000;
                    while (!sshKeyAdded && System.currentTimeMillis() < timeout) {
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        LOGGER.debug("Retry to add SSH Key");
                        formBody = new FormBody.Builder()
                                .add("title", "SSH Key")
                                .add("key", user.getSshPublicKey())
                                .build();
                        request = new Request.Builder().post(formBody).url(gitlabUrl + API_V3_USERS + userId + "/keys").addHeader(PRIVATE_TOKEN, privateToken).build();
                        response = httpClient.newCall(request).execute();
                        LOGGER.debug(response.toString());
                        LOGGER.debug(response.body().string());
                        sshKeyAdded = response.code() == 201 || (response.code() == 400 && body.contains("has already been taken"));
                        if (!sshKeyAdded) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Unable to request gitlab at url {}.", gitlabUrl, e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }

    private static String getAuthenticityToken(String bodyReponse, Pattern pattern) {
        String token = "";
        Matcher matcher = pattern.matcher(bodyReponse);
        if (matcher.find()) {
            token = matcher.group(1);
        }
        return token;
    }

    public static OkHttpClient provideDefaultOkHttpClient() {

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        // Install the all-trusting trust manager
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            CookieManager cookieManager = new CookieManager(new GitlabCookieStore(), CookiePolicy.ACCEPT_ALL);
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .hostnameVerifier((s, sslSession) -> true)
                    .sslSocketFactory(sslSocketFactory)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES)
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .cookieJar(new JavaNetCookieJar(cookieManager));
            return builder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Unable to create Http client", e);
        }
    }

    private static class GitlabCookieStore implements CookieStore {

        private final Map<String, HttpCookie> cache = new HashMap<>();

        @Override
        public void add(URI uri, HttpCookie cookie) {
            cache.put(cookie.getName(), cookie);
        }

        @Override
        public List<HttpCookie> get(URI uri) {
            return new ArrayList<>(cache.values());
        }

        @Override
        public List<HttpCookie> getCookies() {
            return new ArrayList<>(cache.values());
        }

        @Override
        public List<URI> getURIs() {
            return Collections.emptyList();
        }

        @Override
        public boolean remove(URI uri, HttpCookie cookie) {

            return cache.remove(cookie.getName()) != null;
        }

        @Override
        public boolean removeAll() {
            return false;
        }
    }

    private static class OkHttpLogger implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            requireNonNull(chain, "chain must be defined.");
            Request request = chain.request();
            if (LOGGER.isTraceEnabled()) {

            }
            return chain.proceed(request);
        }
    }

}
