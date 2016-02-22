package io.kodokojo.project.gitlab;

import com.google.gson.*;
import com.squareup.okhttp.Response;
import io.kodokojo.commons.project.model.User;
import io.kodokojo.commons.project.model.UserService;
import io.kodokojo.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

import static org.apache.commons.lang.StringUtils.isBlank;

public class GitlabUserManager implements UserManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabUserManager.class);

    private final String privateToken;

    private final GitlabRest gitlabRest;

    public GitlabUserManager(String baseUrl, String privateToken) {
        if (isBlank(baseUrl)) {
            throw new IllegalArgumentException("baseUrl must be defined.");
        }
        if (isBlank(privateToken)) {
            throw new IllegalArgumentException("privateToken must be defined.");
        }
        this.privateToken = privateToken;

        Gson gson = new GsonBuilder().create();
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(baseUrl + "api/v3").setConverter(new GsonConverter(gson)).build();
        gitlabRest = restAdapter.create(GitlabRest.class);
    }

    public boolean createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must be defined.");
        }
        try {
            JsonObject jsonObject = gitlabRest.createUser(privateToken, user.getUsername(), user.getPassword(), user.getEmail(), user.getName(), "false");
            int id = jsonObject.getAsJsonPrimitive("id").getAsInt();

            Response response = gitlabRest.addSshKey(privateToken, Integer.toString(id), "SSH Key", user.getSshPublicKey());
            return response.code() == 201;

        } catch (RetrofitError e) {
            LOGGER.error("unable to complete creation of user : " + e.getBody(), e);
        }
        return false;
    }


    @Override
    public boolean addUser(User user) {
        return false;
    }

    @Override
    public boolean addUserService(UserService userService) {
        return false;
    }

    @Override
    public User getUserByUsername(String username) {
        return null;
    }

    @Override
    public UserService getUserServiceByName(String name) {
        return null;
    }
}
