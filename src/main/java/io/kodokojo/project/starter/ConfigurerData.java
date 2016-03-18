package io.kodokojo.project.starter;

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

import io.kodokojo.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ConfigurerData {

    private final String projectName;
    private final String entrypoint;

    private final User adminUser;

    private final List<User> users;

    private final String domaine;

    private final Map<String, Object> context;

    public ConfigurerData(String projectName, String entrypoint,String domaine, User adminUser, List<User> users) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(entrypoint)) {
            throw new IllegalArgumentException("entrypoint must be defined.");
        }
        if (isBlank(domaine)) {
            throw new IllegalArgumentException("domaine must be defined.");
        }
        if (adminUser == null) {
            throw new IllegalArgumentException("adminUser must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }
        this.projectName = projectName;
        this.entrypoint = entrypoint;
        this.domaine = domaine;
        this.adminUser = adminUser;
        this.users = users;
        this.context = new HashMap<>();
    }

    public String getProjectName() {
        return projectName;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public String getDomaine() {
        return domaine;
    }

    public User getAdminUser() {
        return adminUser;
    }

    public List<User> getUsers() {
        return users;
    }

    public void addInContext(String key, Object data) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("key must be defined.");
        }
        context.put(key, data);
    }

    public Map<String, Object> getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "ConfigurerData{" +
                "projectName='" + projectName + '\'' +
                ",domaine='" + domaine + '\'' +
                ",entrypoint='" + entrypoint + '\'' +
                ", adminUser=" + adminUser +
                ", users=" + users +
                ", context=" + context +
                '}';
    }
}
