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
package io.kodokojo.brick;



import io.kodokojo.model.User;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickConfigurerData {

    private final String projectName;

    private final String entrypoint;

    private final List<User> admins;

    private final List<User> users;

    private final String domaine;

    private final  String stackName;

    private final Map<String, Serializable> context;

    public BrickConfigurerData(String projectName, String stackName, String entrypoint, String domaine, List<User> admins, List<User> users) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(stackName)) {
            throw new IllegalArgumentException("stackName must be defined.");
        }
        if (isBlank(entrypoint)) {
            throw new IllegalArgumentException("entrypoint must be defined.");
        }
        if (isBlank(domaine)) {
            throw new IllegalArgumentException("domaine must be defined.");
        }
        if (admins == null || admins.size() < 1) {
            throw new IllegalArgumentException("admins must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }
        this.projectName = projectName;
        this.stackName = stackName;
        this.entrypoint = entrypoint;
        this.domaine = domaine;
        this.admins = admins;
        this.users = users;
        this.context = new HashMap<>();
    }



    public String getProjectName() {
        return projectName;
    }

    public String getStackName() {
        return stackName;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public String getDomaine() {
        return domaine;
    }

    public User getDefaultAdmin() {
        return admins.get(0);
    }

    public List<User> getAdmins() {
        return admins;
    }

    public List<User> getUsers() {
        return users;
    }

    public void addInContext(String key, Serializable data) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("key must be defined.");
        }
        context.put(key, data);
    }

    public Map<String, Serializable> getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "BrickConfigurerData{" +
                "projectName='" + projectName + '\'' +
                ", entrypoint='" + entrypoint + '\'' +
                ", admins=" + admins +
                ", users=" + users +
                ", domaine='" + domaine + '\'' +
                ", stackName='" + stackName + '\'' +
                ", context=" + context +
                '}';
    }
}
