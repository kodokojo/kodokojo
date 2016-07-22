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
import com.squareup.okhttp.*;
import io.kodokojo.brick.BrickConfigurationException;
import io.kodokojo.brick.BrickConfigurer;
import io.kodokojo.brick.BrickConfigurerData;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.model.User;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;

import javax.inject.Inject;
import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitlabConfigurer implements BrickConfigurer {

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

    public static final String GITLAB_CHANGE_FAIL_MESSAGE = "After a successful password update you will be redirected to login screen.";

    private final BrickUrlFactory brickUrlFactory;

    @Inject
    public GitlabConfigurer(BrickUrlFactory brickUrlFactory) {
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        this.brickUrlFactory = brickUrlFactory;
    }

    @Override
    public BrickConfigurerData configure(BrickConfigurerData brickConfigurerData) throws BrickConfigurationException {
        String gitlabUrl = getGitlabEntryPoint(brickConfigurerData);
        OkHttpClient httpClient = provideDefaultOkHttpClient();

        String changePasswordUrl = getChangePasswordUrl(httpClient, gitlabUrl);
        String resetToken = getResetToken(changePasswordUrl);

        String newPassword = brickConfigurerData.getDefaultAdmin().getPassword();

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
                        throw new BrickConfigurationException("Unable to get private token of root account for gitlab  for project " + brickConfigurerData.getProjectName() + " on url " + gitlabUrl);
                    }
                    brickConfigurerData.addInContext(GITLAB_ADMIN_TOKEN_KEY, authenticityToken);

                    return brickConfigurerData;
                } catch (IOException e) {
                    LOGGER.error("Unable to retrieve account page", e);
                } finally {
                    if (response != null) {
                        try {
                            response.body().close();
                        } catch (IOException e) {
                            LOGGER.debug("Unable to close body response", e);
                        }
                    }
                }
            } else {
                LOGGER.error("Unable to log on Gitlab with new password");
                throw new BrickConfigurationException("Unable to log on Gitlab with new password for project " + brickConfigurerData.getProjectName() + " on url " + gitlabUrl);
            }
        }
        return brickConfigurerData;
    }


    @Override
    public BrickConfigurerData addUsers(BrickConfigurerData brickConfigurerData, List<User> users) throws BrickConfigurationException {
        if (brickConfigurerData == null) {
            throw new IllegalArgumentException("brickConfigurerData must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }

        String gitlabEntryPoint = getGitlabEntryPoint(brickConfigurerData);

        OkHttpClient httpClient = provideDefaultOkHttpClient();
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(gitlabEntryPoint).setClient(new OkClient(httpClient)).build();
        GitlabRest gitlabRest = adapter.create(GitlabRest.class);

        String privateToken = (String) brickConfigurerData.getContext().get(GITLAB_ADMIN_TOKEN_KEY);
        for (User user : users) {
            if (!createUser(gitlabRest, httpClient, gitlabEntryPoint, privateToken, user)) {
                throw new BrickConfigurationException("Unable to create user '" + user.getUsername() + "' for project " + brickConfigurerData.getProjectName() + " on url " + gitlabEntryPoint);
            }
        }

        return brickConfigurerData;
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
            res = response.request().urlString();

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
        Matcher matcher = URL_RESET_TOKEN_PATTERN.matcher(changePasswordUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static boolean changeDefaultPassword(OkHttpClient httpClient, String url, String resetToken, String token, String newPassword) {
        RequestBody formBody = new FormEncodingBuilder()
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
        RequestBody formBody = new FormEncodingBuilder()
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

    private boolean createUser(GitlabRest gitlabRest, OkHttpClient httpClient, String gitlabUrl, String privateToken, User user) {
        Response response = null;
        int id = -1;
        try {
            JsonObject jsonObject = gitlabRest.createUser(privateToken, user.getUsername(), user.getPassword(), user.getEmail(), user.getName(), "false");
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(jsonObject.toString());
            }
            id = jsonObject.getAsJsonPrimitive("id").getAsInt();

        } catch (RetrofitError e) {
            LOGGER.error("Unable to complete creation of user : ", e);
            return false;
        }

        RequestBody formBody = new FormEncodingBuilder()
                .add("title", "SSH Key")
                .add("key", user.getSshPublicKey())
                .build();
        Request request = new Request.Builder().post(formBody).url(gitlabUrl + "/api/v3/users/" + id + "/keys").addHeader("PRIVATE-TOKEN", privateToken).build();
        try {
            response = httpClient.newCall(request).execute();
            boolean res = response.code() >= 200 && response.code() < 300;
            if (response.code() == 500) {
                LOGGER.warn("Gitlab return a 500 while trying to add SSH key for user {} on Gitlab {}.", user.getUsername(), gitlabUrl);
                IOUtils.closeQuietly(response.body());
                request = new Request.Builder().get().url(gitlabUrl + "/api/v3/users/" + id + "/keys").addHeader("PRIVATE-TOKEN", privateToken).build();
                response = httpClient.newCall(request).execute();
                String body = response.body().string();

                JsonParser parser = new JsonParser();
                JsonElement json =  parser.parse(body);
                if (response.code() == 200) {
                    JsonArray keys = (JsonArray) json;
                    res = keys.size() > 0;
                    long timeout = System.currentTimeMillis() + 180000;
                    while (!res && System.currentTimeMillis() < timeout) {
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        LOGGER.debug("Retry to add SSH Key");
                        formBody = new FormEncodingBuilder()
                                .add("title", "SSH Key")
                                .add("key", user.getSshPublicKey())
                                .build();
                        request = new Request.Builder().post(formBody).url(gitlabUrl + "/api/v3/users/" + id + "/keys").addHeader("PRIVATE-TOKEN", privateToken).build();
                        response = httpClient.newCall(request).execute();
                        LOGGER.debug(response.toString());
                        LOGGER.debug( response.body().string());
                        res = response.code() == 201 || (response.code() == 400 && body.contains("has already been taken"));
                        if (!res) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            }
            return res;
        } catch (IOException e) {
            LOGGER.error("Unable to request gitlab at url {}.", gitlabUrl, e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }

        return false;
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
        OkHttpClient httpClient = new OkHttpClient();
        final TrustManager[] certs = new TrustManager[]{new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain,
                                           final String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain,
                                           final String authType) throws CertificateException {
            }
        }};

        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, certs, new SecureRandom());
        } catch (final java.security.GeneralSecurityException ex) {
            //
        }
        httpClient.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        httpClient.setSslSocketFactory(ctx.getSocketFactory());
        CookieManager cookieManager = new CookieManager(new GitlabCookieStore(), CookiePolicy.ACCEPT_ALL);
        httpClient.setCookieHandler(cookieManager);
        httpClient.setReadTimeout(2, TimeUnit.MINUTES);
        httpClient.setConnectTimeout(1, TimeUnit.MINUTES);
        httpClient.setWriteTimeout(1, TimeUnit.MINUTES);
        return httpClient;
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

}
