package io.kodokojo.entrypoint.dto;

import io.kodokojo.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserDto implements Serializable {

    private String identifier;

    private String entityIdentifier;

    private String firstName;

    private String lastName;

    private String name;

    private String username;

    private String email;

    private String password;

    private String sshPublicKey;

    private List<UserProjectConfigIdDto> projectConfigurationIds;

    public UserDto() {
        super();
    }

    public UserDto(User user) {
        this.identifier = user.getIdentifier();
        this.entityIdentifier = user.getEntityIdentifier();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.name = user.getName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.sshPublicKey = user.getSshPublicKey();
        this.projectConfigurationIds = new ArrayList<>();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<UserProjectConfigIdDto> getProjectConfigurationIds() {
        return projectConfigurationIds;
    }

    public void setProjectConfigurationIds(List<UserProjectConfigIdDto> projectConfigurationIds) {
        this.projectConfigurationIds = projectConfigurationIds;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "identifier='" + identifier + '\'' +
                ", entityIdentifier='" + entityIdentifier + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", projectConfigurationIds=" + projectConfigurationIds +
                '}';
    }
}
