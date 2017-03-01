package io.kodokojo.commons.service.repository.search;

import io.kodokojo.commons.model.User;
import io.kodokojo.commons.service.elasticsearch.DataIdProvider;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class UserSearchDto implements DataIdProvider {

    private String identifier;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    public UserSearchDto() {
        super();
    }

    @Override
    public String getId() {
        return identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static UserSearchDto convert(User user) {
        requireNonNull(user, "user must be defined.");
        return converter().apply(user);
    }

    public static Function<User, UserSearchDto> converter() {
        return user -> {
            UserSearchDto res = new UserSearchDto();
            res.setUsername(user.getUsername());
            res.setIdentifier(user.getIdentifier());
            res.setEmail(user.getEmail());
            res.setFirstName(user.getFirstName());
            res.setLastName(user.getLastName());
            return res;
        };
    }

    @Override
    public String toString() {
        return "UserSearchDto{" +
                "identifier='" + identifier + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
