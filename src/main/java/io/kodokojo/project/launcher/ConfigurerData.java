package io.kodokojo.project.launcher;

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

import io.kodokojo.commons.project.model.User;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ConfigurerData {

    private final String entrypoint;

    private final List<User> users;

    public ConfigurerData(String entrypoint, List<User> users) {
        if (isBlank(entrypoint)) {
            throw new IllegalArgumentException("entrypoint must be defined.");
        }
        if (users == null) {
            throw new IllegalArgumentException("users must be defined.");
        }
        this.entrypoint = entrypoint;
        this.users = users;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public List<User> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        return "ConfigurerData{" +
                "entrypoint='" + entrypoint + '\'' +
                ", users=" + users +
                '}';
    }
}
