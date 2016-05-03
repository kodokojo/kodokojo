package io.kodokojo.brick.gitlab;

/*
 * #%L
 * project-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gson.JsonObject;
import com.squareup.okhttp.*;
import io.kodokojo.brick.BrickConfigurerData;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;
import io.kodokojo.brick.BrickConfigurer;
import org.apache.commons.io.IOUtils;
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
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitlabConfigurer implements BrickConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabConfigurer.class);

    public static final String GITLAB_ADMIN_TOKEN_KEY = "GITLAB_ADMIN_API_TOKEN";

    public static final String GITLAB_FORCE_ENTRYPOINT_KEY = "GITLAB_FORCE_USE_DEFAULT_ENTRYPOINT";

    private static final Pattern FORM_TOKEN_PATTERN = Pattern.compile(".*<input type=\"hidden\" name=\"authenticity_token\" value=\"([^\"]*)\" />.*");

    private static final Pattern META_TOKEN_PATTERN = Pattern.compile(".*<meta name=\"csrf-token\" content=\"([^\"]*)\" />.*");

    private static final Pattern PRIVATE_TOKEN_PATTERN = Pattern.compile(".*<input type=\"text\" name=\"token\" id=\"token\" value=\"([^\"]*)\" class=\"form-control\" />.*");

    private static final String SIGNIN_URL = "/users/sign_in";

    private static final String PASSWORD_URL = "/profile/password";

    private static final String PASSWORD_FORM_URL = PASSWORD_URL + "/new";

    private static final String ACCOUNT_URL = "/profile/account";

    private static final String OLD_PASSWORD = "5iveL!fe";

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
    public BrickConfigurerData configure(BrickConfigurerData brickConfigurerData) {
        String gitlabUrl = getGitlabEntryPoint(brickConfigurerData);
        OkHttpClient httpClient = provideDefaultOkHttpClient();
        if (signIn(httpClient, gitlabUrl, ROOT_LOGIN, OLD_PASSWORD)) {
            String token = getAuthenticityToken(httpClient, gitlabUrl + PASSWORD_FORM_URL, META_TOKEN_PATTERN);
            String newPassword = brickConfigurerData.getAdminUser().getPassword();
            if (changePassword(httpClient, gitlabUrl, token, OLD_PASSWORD, newPassword)) {
                if (signIn(httpClient, gitlabUrl, ROOT_LOGIN, newPassword)) {
                    Request request = new Request.Builder().get().url(gitlabUrl + ACCOUNT_URL).build();
                    Response response = null;
                    try {
                        response = httpClient.newCall(request).execute();
                        String body = response.body().string();
                        String authenticityToken = getAuthenticityToken(body, PRIVATE_TOKEN_PATTERN);
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
                }
            } else {
                LOGGER.error("Unable to change root password on entrypoint {}", gitlabUrl);
            }
        } else {
            LOGGER.error("Unable to log as root on entrypoint {}", gitlabUrl);
        }

        return brickConfigurerData;
    }


    @Override
    public BrickConfigurerData addUsers(BrickConfigurerData brickConfigurerData, List<User> users) {
        if (brickConfigurerData == null) {
            throw new IllegalArgumentException("brickConfigurerData must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(getGitlabEntryPoint(brickConfigurerData)).setClient(new OkClient(provideDefaultOkHttpClient())).build();
        GitlabRest gitlabRest = adapter.create(GitlabRest.class);

        for (User user : users) {

            createUser(gitlabRest, (String) brickConfigurerData.getContext().get(GITLAB_ADMIN_TOKEN_KEY), user);
        }

        return brickConfigurerData;
    }

    private String getGitlabEntryPoint( BrickConfigurerData brickConfigurerData) {
        Boolean forceDefault = (Boolean) brickConfigurerData.getContext().get(GITLAB_FORCE_ENTRYPOINT_KEY);
        if (forceDefault != null && forceDefault) {
            return brickConfigurerData.getEntrypoint();
        }
        return "https://" + brickUrlFactory.forgeUrl(brickConfigurerData.getProjectName(), "ci");
    }

    private static boolean changePassword(OkHttpClient httpClient, String gitlabUrl, String token, String oldPassword, String newPassword) {
        RequestBody formBody = new FormEncodingBuilder()
                .addEncoded("utf8", "%E2%9C%93")
                .add("authenticity_token", token)
                .add("user[current_password]", oldPassword)
                .add("user[password]", newPassword)
                .add("user[password_confirmation]", newPassword)
                .build();
        Request request = new Request.Builder()
                .url(gitlabUrl + PASSWORD_URL)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody).build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            String body = response.body().string();
            return response.code() == 200 && !body.contains(GITLAB_CHANGE_FAIL_MESSAGE);
        } catch (IOException e) {
            LOGGER.error("Unable to change the default password", e);
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
            if (response != null && response.body() != null) {
                try {
                    response.body().close();
                } catch (IOException e) {
                    LOGGER.debug("Unable to close body response", e);
                }
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
                try {
                    response.body().close();
                } catch (IOException e) {
                    LOGGER.debug("Unable to close body response", e);
                }
            }
        }
        return null;
    }

    private boolean createUser(GitlabRest gitlabRest, String privateToken, User user) {
        Response response = null;
        try {
            JsonObject jsonObject = gitlabRest.createUser(privateToken, user.getUsername(), user.getPassword(), user.getEmail(), user.getName(), "false");
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(jsonObject.toString());
            }

            int id = jsonObject.getAsJsonPrimitive("id").getAsInt();

            response = gitlabRest.addSshKey(privateToken, Integer.toString(id), "SSH Key", user.getSshPublicKey());
            return response.code() == 201;

        } catch (RetrofitError e) {
            LOGGER.error("unable to complete creation of user : ", e);
            if (LOGGER.isTraceEnabled()) {
                InputStream in = null;
                try {
                    in = e.getResponse().getBody().in();
                    LOGGER.trace(IOUtils.toString(in));
                    IOUtils.closeQuietly(in);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
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
